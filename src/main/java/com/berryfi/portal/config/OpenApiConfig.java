package com.berryfi.portal.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class OpenApiConfig {

    @Value("${app.base-url}")
    private String baseUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .addServersItem(new Server().url(baseUrl))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(
                        new Components()
                                .addSecuritySchemes(securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                )
                .info(new Info().title("BerryFi API").version("v1"))
                .tags(Arrays.asList(
                        new Tag().name("Analytics").description("Analytics and reporting operations"),
                        new Tag().name("Audit").description("Audit log operations"),
                        new Tag().name("Authentication").description("User authentication operations"),
                        new Tag().name("Billing").description("Billing and payment operations"),
                        new Tag().name("Dashboard").description("Dashboard and summary operations"),
                        new Tag().name("Projects").description("Project management operations"),
                        new Tag().name("Reports").description("Report generation operations"),
                        new Tag().name("Team").description("Team member management operations"),
                        new Tag().name("Usage").description("Usage tracking operations")

                ));
    }
}
