package com.trajkovski.emailnotification.kafka;

import com.trajkovski.core.ProductCreatedEvent;
import com.trajkovski.emailnotification.error.NonRetryableException;
import com.trajkovski.emailnotification.error.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
public class ProductCreatedEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductCreatedEventHandler.class);

    private final RestTemplate restTemplate;

    public ProductCreatedEventHandler(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @KafkaListener(topics = "product-created-events-topic", groupId = "product-created-events")
    public void handle(ProductCreatedEvent productCreatedEvent) {
        LOGGER.info("Received new event: {}", productCreatedEvent);

        String requestUrl = "http://localhost:8082/products/" + productCreatedEvent.getProductId();

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, productCreatedEvent, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                LOGGER.info("Successfully posted event: {}", productCreatedEvent);
            }

        } catch (ResourceAccessException e) {
            LOGGER.error("Failed to post event: {}", productCreatedEvent);
            throw new RetryableException(e);
        } catch (HttpServerErrorException e) {
            LOGGER.error("Failed to post event: {}", productCreatedEvent);
            throw new NonRetryableException(e);
        }
    }
}
