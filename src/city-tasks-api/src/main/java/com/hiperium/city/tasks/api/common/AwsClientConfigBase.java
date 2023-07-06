package com.hiperium.city.tasks.api.common;

import com.hiperium.city.tasks.api.vo.AwsProperties;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.core.client.builder.SdkClientBuilder;

import java.net.URI;
import java.util.Objects;

public class AwsClientConfigBase {

    protected AwsProperties awsProperties;

    protected void configureCredentials(final AwsClientBuilder awsClientBuilder) {
        if (Objects.isNull(this.awsProperties.getAccessKeyId()) ||
                Objects.isNull(this.awsProperties.getSecretAccessKey())) {
            awsClientBuilder.credentialsProvider(EnvironmentVariableCredentialsProvider.create());
        } else {
            awsClientBuilder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(this.awsProperties.getAccessKeyId(),
                            this.awsProperties.getSecretAccessKey())
            ));
        }
    }

    protected void configureEndpoint(SdkClientBuilder sdkClientBuilder) {
        if (Objects.nonNull(this.awsProperties.getEndpointOverride()) &&
                !this.awsProperties.getEndpointOverride().isBlank()) {
            sdkClientBuilder.endpointOverride(URI.create(this.awsProperties.getEndpointOverride()));
        }
    }
}
