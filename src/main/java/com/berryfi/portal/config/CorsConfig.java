package com.berryfi.portal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                String[] origins = allowedOrigins.split(",");
                if ("*".equals(allowedOrigins)) {
                    registry.addMapping("/api/**")
                            .allowedOriginPatterns("*")
                            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                            .allowedHeaders("*")
                            .allowCredentials(true)
                            .maxAge(3600);
                } else {
                    registry.addMapping("/api/**")
                            .allowedOrigins(origins)
                            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                            .allowedHeaders("*")
                            .allowCredentials(true)
                            .maxAge(3600);
                }
            }
        };
    }
}
