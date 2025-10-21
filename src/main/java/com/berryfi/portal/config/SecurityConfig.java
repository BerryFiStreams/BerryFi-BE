package com.berryfi.portal.config;

import com.berryfi.portal.security.CustomPermissionEvaluator;
import com.berryfi.portal.security.JwtAuthenticationFilter;
import com.berryfi.portal.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for the application.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomPermissionEvaluator permissionEvaluator;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    public SecurityConfig(CustomUserDetailsService userDetailsService, 
                         CustomPermissionEvaluator permissionEvaluator,
                         JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.permissionEvaluator = permissionEvaluator;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(permissionEvaluator);
        return expressionHandler;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - no authentication required
                .requestMatchers(
                    "/api/auth/login", 
                    "/api/auth/refresh", 
                    "/api-docs/**", 
                    "/swagger-ui/**", 
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/track/**"  // Tracking URLs should be publicly accessible
                    ).permitAll()
                .requestMatchers("/api/test/public").permitAll() // Public test endpoint
                .requestMatchers("/h2-console/**").permitAll() // H2 console for development
                .requestMatchers("/actuator/health").permitAll() // Health check
                .requestMatchers("/api/vm/**").permitAll() // VM controller endpoints - no authentication required
                
                // Tenant configuration endpoints - public access for initial page load
                .requestMatchers("/api/tenant/config").permitAll() // Get tenant config (context-based)
                .requestMatchers("/api/tenant/config/*").permitAll() // Get tenant config by subdomain
                .requestMatchers("/api/tenant/check-subdomain").permitAll() // Check subdomain availability
                
                // Invitation endpoints - public access for specific operations
                .requestMatchers(HttpMethod.GET, "/api/invitations/*").permitAll() // Get invitation details by token
                .requestMatchers(HttpMethod.POST, "/api/invitations/register").permitAll() // Register through invitation
                .requestMatchers(HttpMethod.POST, "/api/invitations/*/decline").permitAll() // Decline invitation
                .requestMatchers(HttpMethod.POST, "/api/invitations/*/resend").permitAll() // Resend invitation
                
                // Team invitation endpoints - public access for invitation token operations
                .requestMatchers(HttpMethod.GET, "/api/team/invitations/token/*").permitAll() // Get invitation details by token
                .requestMatchers(HttpMethod.GET, "/api/team/invitations/check-user").permitAll() // Check if user exists by email
                .requestMatchers(HttpMethod.POST, "/api/team/invitations/register").permitAll() // Register through team invitation
                .requestMatchers(HttpMethod.POST, "/api/team/invitations/token/*/accept").permitAll() // Accept invitation by token
                .requestMatchers(HttpMethod.GET, "/api/team/members/invitations/*").permitAll() // Alternative endpoint for invitation details
                
                // Authentication required endpoints
                .requestMatchers("/api/auth/me", "/api/auth/logout").authenticated()
                
                // API endpoints with role-based access
                .requestMatchers("/api/projects/**").hasAnyRole("SUPER_ADMIN", "ORG_OWNER", "ORG_ADMIN", "ORG_MEMBER")
                .requestMatchers("/api/billing/**").hasAnyRole("SUPER_ADMIN", "ORG_OWNER", "ORG_ADMIN", "ORG_BILLING")
                .requestMatchers("/api/team/**").hasAnyRole("SUPER_ADMIN", "ORG_OWNER", "ORG_ADMIN", "ORG_MEMBER")
                
                .requestMatchers("/api/analytics/**").hasAnyRole("SUPER_ADMIN", "ORG_OWNER", "ORG_ADMIN", "ORG_REPORTER")
                .requestMatchers("/api/audit/**").hasAnyRole("SUPER_ADMIN", "ORG_OWNER", "ORG_ADMIN", "ORG_AUDITOR")
                .requestMatchers("/api/usage/**").hasAnyRole("SUPER_ADMIN", "ORG_OWNER", "ORG_ADMIN", "ORG_AUDITOR")
                .requestMatchers("/api/reports/**").hasAnyRole("SUPER_ADMIN", "ORG_OWNER", "ORG_ADMIN", "ORG_REPORTER")
                
                // Allow all non-API requests (static files, frontend routes, etc.)
                .requestMatchers(request -> !request.getRequestURI().startsWith("/api/")).permitAll()
                
                // All other requests (remaining /api/* endpoints) require authentication
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // H2 console configuration for development
        http.headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
