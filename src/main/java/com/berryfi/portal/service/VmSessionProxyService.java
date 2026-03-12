package com.berryfi.portal.service;

import com.berryfi.portal.context.TenantContext;
import com.berryfi.portal.entity.VmInstance;
import com.berryfi.portal.entity.VmSession;
import com.berryfi.portal.enums.SessionStatus;
import com.berryfi.portal.repository.VmInstanceRepository;
import com.berryfi.portal.repository.VmSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class VmSessionProxyService {

    private static final Logger logger = LoggerFactory.getLogger(VmSessionProxyService.class);

    private static final Set<String> REQUEST_HEADERS_TO_SKIP = Set.of(
        "host",
        "connection",
        "content-length",
        "transfer-encoding",
        "accept-encoding"
    );

    private static final Set<String> RESPONSE_HEADERS_TO_SKIP = Set.of(
        "connection",
        "content-length",
        "transfer-encoding"
    );

    private static final Pattern HTML_ROOT_PATH_ATTRIBUTES = Pattern.compile(
        "(?i)(\\b(?:href|src|action|poster)=['\"])\\/(?!\\/)"
    );

    private static final Pattern HTML_ROOT_PATH_UNQUOTED_ATTRIBUTES = Pattern.compile(
        "(?i)(\\b(?:href|src|action|poster)=)\\/(?!\\/)"
    );

    private static final Pattern HTML_ROOT_PATH_URLS = Pattern.compile(
        "(?i)(url\\(['\"]?)\\/(?!\\/)"
    );

    @Autowired
    private VmSessionRepository vmSessionRepository;

    @Autowired
    private VmInstanceRepository vmInstanceRepository;

    public String buildProxyPath(String sessionId) {
        return "/vm/session/" + sessionId + "/";
    }

    public ResolvedVmTarget resolveTarget(String sessionId) {
        String resolvedSessionId = Objects.requireNonNull(sessionId, "sessionId");
        Optional<VmSession> sessionOpt = vmSessionRepository.findById(resolvedSessionId);
        if (sessionOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "VM session not found");
        }

        VmSession session = sessionOpt.get();
        validateTenantAccess(session);

        if (session.getStatus() == SessionStatus.FAILED ||
            session.getStatus() == SessionStatus.TERMINATED ||
            session.getStatus() == SessionStatus.TERMINATING ||
            session.getStatus() == SessionStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.GONE, "VM session is no longer available");
        }

        URI targetUri = getTargetBaseUri(session);
        if (targetUri == null) {
            throw new ResponseStatusException(HttpStatus.LOCKED, "VM session target is not ready yet");
        }

        return new ResolvedVmTarget(session, targetUri);
    }

    public void proxy(HttpServletRequest request, HttpServletResponse response, String sessionId, String pathSuffix) throws IOException {
        ResolvedVmTarget target = resolveTarget(sessionId);
        URI upstreamUri = buildUpstreamUri(target, pathSuffix, request.getQueryString());
        String proxyBasePath = buildProxyPath(sessionId);

        logger.debug("Proxying VM session {} request {} {} to {}", sessionId, request.getMethod(), request.getRequestURI(), upstreamUri);

        HttpURLConnection connection = null;
        int maxRetries = 5;
        long delay = 1000; // 1 second

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                connection = (HttpURLConnection) upstreamUri.toURL().openConnection();
                connection.setRequestMethod(request.getMethod());
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(60000);
                connection.setInstanceFollowRedirects(false);

                copyRequestHeaders(request, connection);

                if (hasRequestBody(request.getMethod())) {
                    connection.setDoOutput(true);
                    request.getInputStream().transferTo(connection.getOutputStream());
                }

                int status = connection.getResponseCode();
                response.setStatus(status);
                response.setHeader("Cache-Control", "no-store");
                copyResponseHeaders(connection, response, target, proxyBasePath);

                if (!"HEAD".equalsIgnoreCase(request.getMethod())) {
                    byte[] responseBody = readResponseBody(connection);
                    if (responseBody.length > 0 && shouldRewriteHtml(connection)) {
                        responseBody = rewriteHtmlResponse(responseBody, connection, proxyBasePath);
                    }

                    if (responseBody.length > 0) {
                        response.setContentLength(responseBody.length);
                        response.getOutputStream().write(responseBody);
                    }
                }
                return; // Success, exit the loop

            } catch (IOException e) {
                if (attempt == maxRetries) {
                    StringBuilder headers = new StringBuilder();
                    request.getHeaderNames().asIterator().forEachRemaining(name ->
                        headers.append(name).append(": ").append(request.getHeader(name)).append("\n"));
                    logger.error("Proxy error for session {} on final attempt {}: upstream URI {}, method {}. Request headers:\n{}",
                        sessionId, attempt, upstreamUri, request.getMethod(), headers.toString(), e);
                    if (!response.isCommitted()) {
                        response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Proxy connection failed after " + maxRetries + " attempts: " + e.getMessage());
                    }
                    return; // Exit after final failure
                }

                logger.warn("Proxy attempt {}/{} for session {} failed. Retrying in {}ms. Error: {}",
                    attempt, maxRetries, sessionId, delay, e.getMessage());

                try {
                    Thread.sleep(delay);
                    delay *= 2; // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Proxy retry was interrupted");
                    return;
                }

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }

    private void validateTenantAccess(VmSession session) {
        if (!TenantContext.hasTenantContext()) {
            return;
        }

        String currentProjectId = TenantContext.getProjectId();
        if (currentProjectId == null || !currentProjectId.equals(session.getProjectId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "VM session does not belong to this tenant");
        }
    }

    private URI getTargetBaseUri(VmSession session) {
        String targetUrl = session.getConnectionUrl();
        if (targetUrl != null && !targetUrl.isBlank()) {
            try {
                return new URI(targetUrl);
            } catch (URISyntaxException e) {
                logger.error("Invalid session connection URL for {}: {}", session.getId(), targetUrl, e);
            }
        }

        if (session.getVmIpAddress() != null && session.getVmPort() != null) {
            return URI.create(String.format("http://%s:%d/", session.getVmIpAddress(), session.getVmPort()));
        }

        if (session.getVmInstanceId() != null) {
            String vmInstanceId = session.getVmInstanceId();
            Optional<VmInstance> vmOpt = vmInstanceRepository.findById(Objects.requireNonNull(vmInstanceId));
            if (vmOpt.isPresent()) {
                VmInstance vm = vmOpt.get();
                if (vm.getConnectionUrl() != null && !vm.getConnectionUrl().isBlank()) {
                    return URI.create(vm.getConnectionUrl());
                }
                if (vm.getIpAddress() != null && vm.getPort() != null) {
                    return URI.create(String.format("http://%s:%d/", vm.getIpAddress(), vm.getPort()));
                }
            }
        }

        return null;
    }

    private URI buildUpstreamUri(ResolvedVmTarget target, String pathSuffix, String requestQuery) {
        URI baseUri = target.baseUri();
        String normalizedSuffix = pathSuffix == null ? "" : pathSuffix.replaceFirst("^/+", "");
        String basePath = baseUri.getPath();
        if (basePath == null || basePath.isBlank()) {
            basePath = "/";
        }

        String finalPath;
        if (normalizedSuffix.isEmpty()) {
            finalPath = basePath;
        } else if (basePath.endsWith("/")) {
            finalPath = basePath + normalizedSuffix;
        } else {
            finalPath = basePath + "/" + normalizedSuffix;
        }

        List<String> queryParts = new ArrayList<>();
        if (baseUri.getQuery() != null && !baseUri.getQuery().isBlank()) {
            queryParts.add(baseUri.getQuery());
        }
        if (requestQuery != null && !requestQuery.isBlank()) {
            queryParts.add(requestQuery);
        }

        String mergedQuery = queryParts.isEmpty() ? null : String.join("&", queryParts);

        try {
            return new URI(
                baseUri.getScheme(),
                null,
                baseUri.getHost(),
                baseUri.getPort(),
                finalPath,
                mergedQuery,
                null
            );
        } catch (URISyntaxException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to build upstream VM request", e);
        }
    }

    private void copyRequestHeaders(HttpServletRequest request, HttpURLConnection connection) {
        var headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String normalized = headerName.toLowerCase(Locale.ROOT);
            if (REQUEST_HEADERS_TO_SKIP.contains(normalized)) {
                continue;
            }

            var headerValues = request.getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                connection.addRequestProperty(headerName, headerValues.nextElement());
            }
        }

        connection.setRequestProperty("X-Forwarded-Proto", request.getScheme());
        connection.setRequestProperty("X-Forwarded-Host", request.getHeader("Host"));
        connection.setRequestProperty("X-Forwarded-For", request.getRemoteAddr());
    }

    private void copyResponseHeaders(HttpURLConnection connection, HttpServletResponse response, ResolvedVmTarget target, String proxyBasePath) {
        for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
            String headerName = entry.getKey();
            if (headerName == null) {
                continue;
            }

            if (RESPONSE_HEADERS_TO_SKIP.contains(headerName.toLowerCase(Locale.ROOT))) {
                continue;
            }

            for (String value : entry.getValue()) {
                response.addHeader(headerName, rewriteResponseHeaderValue(headerName, value, target.baseUri(), proxyBasePath));
            }
        }
    }

    private boolean hasRequestBody(String method) {
        return !("GET".equalsIgnoreCase(method) ||
            "HEAD".equalsIgnoreCase(method) ||
            "OPTIONS".equalsIgnoreCase(method));
    }

    private InputStream getResponseStream(HttpURLConnection connection) throws IOException {
        int status = connection.getResponseCode();
        return status >= 400 ? connection.getErrorStream() : connection.getInputStream();
    }

    private byte[] readResponseBody(HttpURLConnection connection) throws IOException {
        try (InputStream responseStream = getResponseStream(connection)) {
            if (responseStream == null) {
                return new byte[0];
            }
            return responseStream.readAllBytes();
        }
    }

    private boolean shouldRewriteHtml(HttpURLConnection connection) {
        String contentType = connection.getContentType();
        return contentType != null && contentType.toLowerCase(Locale.ROOT).contains("text/html");
    }

    private byte[] rewriteHtmlResponse(byte[] responseBody, HttpURLConnection connection, String proxyBasePath) {
        Charset charset = resolveCharset(connection.getContentType());
        String html = new String(responseBody, charset);
        String rewrittenHtml = rewriteHtmlPaths(html, proxyBasePath);
        return rewrittenHtml.getBytes(charset);
    }

    private Charset resolveCharset(String contentType) {
        if (contentType != null) {
            String[] segments = contentType.split(";");
            for (String segment : segments) {
                String trimmedSegment = segment.trim();
                if (trimmedSegment.toLowerCase(Locale.ROOT).startsWith("charset=")) {
                    String charsetName = trimmedSegment.substring("charset=".length()).trim();
                    try {
                        return Charset.forName(charsetName);
                    } catch (Exception ex) {
                        logger.warn("Unsupported upstream charset '{}', defaulting to UTF-8", charsetName);
                    }
                }
            }
        }
        return StandardCharsets.UTF_8;
    }

    String rewriteHtmlPaths(String html, String proxyBasePath) {
        String rewrittenHtml = HTML_ROOT_PATH_ATTRIBUTES.matcher(html)
            .replaceAll("$1" + proxyBasePath);
        rewrittenHtml = HTML_ROOT_PATH_UNQUOTED_ATTRIBUTES.matcher(rewrittenHtml)
            .replaceAll("$1" + proxyBasePath);
        rewrittenHtml = HTML_ROOT_PATH_URLS.matcher(rewrittenHtml)
            .replaceAll("$1" + proxyBasePath);

        if (!rewrittenHtml.toLowerCase(Locale.ROOT).contains("<base ")) {
            rewrittenHtml = rewrittenHtml.replaceFirst(
                "(?i)<head([^>]*)>",
                "<head$1><base href=\"" + proxyBasePath + "\">"
            );
        }

        return rewrittenHtml;
    }

    private String rewriteResponseHeaderValue(String headerName, String value, URI baseUri, String proxyBasePath) {
        if (value == null) {
            return null;
        }

        if ("Location".equalsIgnoreCase(headerName)) {
            return rewriteLocationHeader(value, baseUri, proxyBasePath);
        }

        if ("Set-Cookie".equalsIgnoreCase(headerName)) {
            return value.replace("Path=/", "Path=" + proxyBasePath);
        }

        return value;
    }

    private String rewriteLocationHeader(String value, URI baseUri, String proxyBasePath) {
        if (value.startsWith("/")) {
            return proxyBasePath + value.substring(1);
        }

        try {
            URI locationUri = new URI(value);
            boolean matchesUpstream = Objects.equals(locationUri.getScheme(), baseUri.getScheme())
                && Objects.equals(locationUri.getHost(), baseUri.getHost())
                && locationUri.getPort() == baseUri.getPort();

            if (!matchesUpstream) {
                return value;
            }

            StringBuilder rewritten = new StringBuilder(proxyBasePath);
            String path = locationUri.getPath();
            if (path != null && !path.isBlank()) {
                rewritten.append(path.startsWith("/") ? path.substring(1) : path);
            }
            if (locationUri.getQuery() != null && !locationUri.getQuery().isBlank()) {
                rewritten.append('?').append(locationUri.getQuery());
            }
            if (locationUri.getFragment() != null && !locationUri.getFragment().isBlank()) {
                rewritten.append('#').append(locationUri.getFragment());
            }
            return rewritten.toString();
        } catch (URISyntaxException ex) {
            logger.debug("Unable to rewrite upstream Location header '{}': {}", value, ex.getMessage());
            return value;
        }
    }

    public record ResolvedVmTarget(VmSession session, URI baseUri) {}
}