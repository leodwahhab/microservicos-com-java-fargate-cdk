package com.example.aws_project01.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.awscore.endpoint.AwsClientEndpointProvider;
import software.amazon.awssdk.endpoints.EndpointProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.Topic;

import java.net.URI;

@Configuration
@Profile("local")
public class SnsCreate {
    private static final Logger LOG = LoggerFactory.getLogger(SnsCreate.class);

    private final SnsClient snsClient;
    private final String productEventsTopic;

    public SnsCreate() {
        this.snsClient = SnsClient.builder()
                .endpointOverride(URI.create("http://localhost:4566"))
                .region(Region.US_EAST_1)
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .build();

        CreateTopicRequest createTopicRequest = CreateTopicRequest.builder().name("product-events").build();
        this.productEventsTopic = this.snsClient.createTopic(createTopicRequest).topicArn();

        LOG.info("SNS topic ARN: {}", this.productEventsTopic);
    }

    @Bean
    public SnsClient snsClient() {
        return this.snsClient;
    }

    @Bean(name = "productEventsTopic")
    public Topic snsProductEventsTopic() {
        return Topic.builder().topicArn(productEventsTopic).build();
    }
}
