package com.berryfi.portal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
public class Webconfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Serve assets folder specifically - this is critical for Vite builds
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/")
                .setCachePeriod(31556926) // 1 year cache for assets
                .resourceChain(false); // Disable resource chain for direct serving

        // Serve static resources (CSS, JS, images) from static folder
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(31556926) // 1 year cache for static assets
                .resourceChain(false); // Disable resource chain for direct serving

        // Serve root level static files (like vite.svg, favicon.ico)
        registry.addResourceHandler("/*.svg", "/*.ico", "/*.png", "/*.jpg", "/*.jpeg", "/*.gif", "/*.js", "/*.css")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(31556926)
                .resourceChain(false); // Disable resource chain for direct serving

        // Handle React Router - this comes last and has lowest priority
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    @Nullable
                    protected Resource getResource(@NonNull String resourcePath, @NonNull Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        
                        // If the requested resource exists, serve it
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }
                        
                        // For React Router: if resource doesn't exist and it's not an API call or static asset,
                        // serve index.html (SPA fallback)
                        if (!resourcePath.startsWith("api/") && 
                            !resourcePath.startsWith("assets/") && 
                            !resourcePath.startsWith("static/") &&
                            !resourcePath.startsWith("actuator/") &&
                            !resourcePath.contains(".")) {
                            return new ClassPathResource("/static/index.html");
                        }
                        
                        return null;
                    }
                });
    }
}
