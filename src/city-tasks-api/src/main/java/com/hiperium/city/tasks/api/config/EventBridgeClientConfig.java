package com.hiperium.city.tasks.api.config;

import com.hiperium.city.tasks.api.logger.HiperiumLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.EventBridgeClientBuilder;

import java.net.URI;
import java.util.Objects;

@Configuration
public class EventBridgeClientConfig {

    private static final HiperiumLogger LOGGER = HiperiumLogger.getLogger(EventBridgeClientConfig.class);

    @Value("${aws.endpoint-override}")
    private String endpointOverride;

    @Bean
    public EventBridgeClient eventBridgeClient() {
        if (Objects.isNull(this.endpointOverride) || this.endpointOverride.isEmpty()) {
            return EventBridgeClient.create();
        }
        LOGGER.debug("EventBridge Endpoint: {}", this.endpointOverride);
        var regionProvider = DefaultAwsRegionProviderChain.builder().build();
        EventBridgeClientBuilder eventBridgeClientBuilder = EventBridgeClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(regionProvider.getRegion());
        eventBridgeClientBuilder.endpointOverride(URI.create(this.endpointOverride));
        return eventBridgeClientBuilder.build();
    }
}
