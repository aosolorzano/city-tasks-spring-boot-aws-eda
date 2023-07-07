package com.hiperium.city.tasks.api.config;

import com.hiperium.city.tasks.api.logger.HiperiumLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClientBuilder;

import java.net.URI;
import java.util.Objects;

@Configuration
public class DynamoDBClientConfig {

    private static final HiperiumLogger LOGGER = HiperiumLogger.getLogger(DynamoDBClientConfig.class);

    @Value("${aws.endpoint-override}")
    private String endpointOverride;

    @Bean
    public DynamoDbAsyncClient dynamoDbAsyncClient() {
        if (Objects.isNull(this.endpointOverride) || this.endpointOverride.isEmpty()) {
            return DynamoDbAsyncClient.create();
        }
        LOGGER.debug("DynamoDB Endpoint: {}", this.endpointOverride);
        var dynamoDbClientBuilder = getDynamoDbAsyncClientBuilder(this.endpointOverride);
        return dynamoDbClientBuilder.build();
    }

    public static DynamoDbAsyncClientBuilder getDynamoDbAsyncClientBuilder(String endpointOverride) {
        var regionProvider = DefaultAwsRegionProviderChain.builder().build();
        var dynamoDbClientBuilder = DynamoDbAsyncClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(regionProvider.getRegion());
        dynamoDbClientBuilder.endpointOverride(URI.create(endpointOverride));
        return dynamoDbClientBuilder;
    }
}
