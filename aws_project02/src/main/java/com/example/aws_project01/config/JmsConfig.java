package com.example.aws_project01.config;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import jakarta.jms.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@EnableJms
public class JmsConfig {

    @Value("${aws.region}")
    private String awsRegion;

    private SQSConnectionFactory sqsConnectionFactory;

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
       sqsConnectionFactory = new SQSConnectionFactory(
               new ProviderConfiguration(),
               SqsClient.builder().region(Region.of(awsRegion))
                       .credentialsProvider(AwsCredentialsProviderChain.builder().build())
                       .build());

       DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
       factory.setConnectionFactory(sqsConnectionFactory);
       factory.setDestinationResolver(new DynamicDestinationResolver());
       factory.setConcurrency("2");
       factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);

       return factory;
    }
}
