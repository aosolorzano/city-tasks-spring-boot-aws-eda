package com.hiperium.city.tasks.api.config;

import com.hiperium.city.tasks.api.common.AwsClientConfigBase;
import com.hiperium.city.tasks.api.logger.HiperiumLogger;
import com.hiperium.city.tasks.api.vo.AwsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClientBuilder;

@Configuration
public class DynamoDBClientConfig extends AwsClientConfigBase {

    private static final HiperiumLogger LOGGER = HiperiumLogger.getLogger(DynamoDBClientConfig.class);

    public DynamoDBClientConfig(AwsProperties awsProperties) {
        super.awsProperties = awsProperties;
    }

    @Bean
    public DynamoDbAsyncClient dynamoDbAsyncClient() {
        LOGGER.debug("DynamoDB Endpoint Override: {}", this.awsProperties.getEndpointOverride());
        LOGGER.debug("DynamoDB Region: {}", this.awsProperties.getRegion());
        DynamoDbAsyncClientBuilder dynamoDbClientBuilder = DynamoDbAsyncClient.builder()
                .region(Region.of(this.awsProperties.getRegion()));
        super.configureCredentials(dynamoDbClientBuilder);
        super.configureEndpoint(dynamoDbClientBuilder);
        return dynamoDbClientBuilder.build();
    }
}
