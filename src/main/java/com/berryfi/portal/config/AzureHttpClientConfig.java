package com.berryfi.portal.config;

import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

/**
 * Configuration for Azure SDK HTTP client settings.
 * Addresses issues with large HTTP headers (especially Azure auth tokens).
 */
@Configuration
public class AzureHttpClientConfig {
    
    @Value("${azure.core.http.netty.max-header-size:65536}")
    private int maxHeaderSize;
    
    @Value("${azure.api.timeout.seconds:60}")
    private int timeoutSeconds;
    
    /**
     * Creates a custom Azure HttpClient with increased header size limits.
     * This bean will be automatically used by all Azure SDK clients.
     */
    @Bean
    public HttpClient azureHttpClient() {
        // Create connection provider with proper configuration
        ConnectionProvider provider = ConnectionProvider.builder("azure-connection-pool")
            .maxConnections(100)
            .maxIdleTime(Duration.ofSeconds(30))
            .maxLifeTime(Duration.ofMinutes(5))
            .pendingAcquireTimeout(Duration.ofSeconds(45))
            .build();
        
        // Create reactor-netty HTTP client with custom decoder settings for large headers
        reactor.netty.http.client.HttpClient reactorHttpClient = reactor.netty.http.client.HttpClient
            .create(provider)
            .responseTimeout(Duration.ofSeconds(timeoutSeconds))
            .httpResponseDecoder(spec -> spec
                .maxHeaderSize(maxHeaderSize)  // THIS IS THE KEY FIX - increase max header size
                .maxInitialLineLength(4096)
            );
        
        // Wrap in Azure's HttpClient
        HttpClient httpClient = new NettyAsyncHttpClientBuilder(reactorHttpClient)
            .build();
        
        System.out.println("✓ Azure HTTP client bean configured with max header size: " + maxHeaderSize + " bytes");
        
        return httpClient;
    }
}
