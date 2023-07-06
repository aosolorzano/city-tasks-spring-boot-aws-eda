package com.hiperium.city.tasks.api.config;

import com.hiperium.city.tasks.api.common.AwsClientConfigBase;
import com.hiperium.city.tasks.api.logger.HiperiumLogger;
import com.hiperium.city.tasks.api.vo.AwsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.EventBridgeClientBuilder;

@Configuration
public class EventBridgeClientConfig extends AwsClientConfigBase {

    private static final HiperiumLogger LOGGER = HiperiumLogger.getLogger(EventBridgeClientConfig.class);

    public EventBridgeClientConfig(AwsProperties awsProperties) {
        super.awsProperties = awsProperties;
    }

    @Bean
    public EventBridgeClient eventBridgeClient() {
        LOGGER.debug("EventBridge Endpoint Override: {}", this.awsProperties.getEndpointOverride());
        LOGGER.debug("EventBridge Region: {}", this.awsProperties.getRegion());
        EventBridgeClientBuilder eventBridgeClientBuilder = EventBridgeClient.builder()
                .region(Region.of(awsProperties.getRegion()));
        super.configureCredentials(eventBridgeClientBuilder);
        super.configureEndpoint(eventBridgeClientBuilder);
        return eventBridgeClientBuilder.build();
    }
}
