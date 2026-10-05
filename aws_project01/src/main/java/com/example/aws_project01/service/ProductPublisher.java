package com.example.aws_project01.service;

import com.example.aws_project01.enums.EventType;
import com.example.aws_project01.model.Envelope;
import com.example.aws_project01.model.Product;
import com.example.aws_project01.model.ProductEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.json.JsonParseException;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.Topic;
import tools.jackson.databind.ObjectMapper;

@Service
public class ProductPublisher {
    private static final Logger LOG = LoggerFactory.getLogger(ProductPublisher.class);
    private final SnsClient snsClient;
    private final Topic productEventsTopic;
    private final ObjectMapper objectMapper;

    public ProductPublisher(SnsClient snsClient, @Qualifier("productEventsTopic") Topic productEventsTopic, ObjectMapper objectMapper) {
        this.snsClient = snsClient;
        this.productEventsTopic = productEventsTopic;
        this.objectMapper = objectMapper;
    }

    public void publishProductEvent(Product product, EventType eventType, String username) {
        ProductEvent productEvent = new ProductEvent(product.getId(), product.getCode(), username);

        try {
            String data = objectMapper.writeValueAsString(productEvent);
            Envelope envelope = new Envelope(eventType, data);
            snsClient.publish(PublishRequest.builder()
                            .topicArn(productEventsTopic.topicArn())
                            .message(objectMapper.writeValueAsString(envelope))
                    .build());
        } catch (JsonParseException e) {
            LOG.error("Failed to create product event message");
        }
    }
}
