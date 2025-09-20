package com.berryfi.portal.security;

import com.berryfi.portal.service.CustomUserDetailsService;
import com.berryfi.portal.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter to validate JWT tokens in requests.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Check if Authorization header is present and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token
        jwt = authHeader.substring(7);

        // Create a wrapper for the request
        JwtRequestWrapper requestWrapper = new JwtRequestWrapper(request);

        try {
            // Extract username from JWT
            userEmail = jwtService.extractUsername(jwt);

            // If username is found and no authentication is set in security context
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Load user details
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                // Validate token
                if (jwtService.validateToken(jwt, userDetails)) {
                    
                    // Extract additional claims from JWT
                    String organizationId = jwtService.getOrganizationIdFromToken(jwt);
                    String workspaceId = jwtService.getWorkspaceIdFromToken(jwt);
                    String userId = jwtService.getUserIdFromToken(jwt);
                    
                    // Add claims as headers to the wrapper
                    if (organizationId != null) {
                        requestWrapper.putHeader("X-Organization-ID", organizationId);
                    }
                    if (workspaceId != null) {
                        requestWrapper.putHeader("X-Workspace-ID", workspaceId);
                    }
                    if (userId != null) {
                        requestWrapper.putHeader("X-User-ID", userId);
                    }
                    
                    // Create authentication token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    
                    // Set authentication details
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(requestWrapper));
                    
                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Log the exception but don't stop the filter chain
            logger.error("Cannot set user authentication: " + e.getMessage());
        }

        filterChain.doFilter(requestWrapper, response);
    }
}
