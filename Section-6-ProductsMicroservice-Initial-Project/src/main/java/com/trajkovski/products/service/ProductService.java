package com.trajkovski.products.service;

import com.trajkovski.core.ProductCreatedEvent;
import com.trajkovski.products.ctrl.request.CreateProductRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class ProductService {

    private final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);

    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;

    public ProductService(KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public String createProduct(CreateProductRequest createProductRequest) throws ExecutionException, InterruptedException {
        String productId = UUID.randomUUID().toString();
        ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent(
                productId,
                createProductRequest.getName(),
                createProductRequest.getPrice(),
                createProductRequest.getQuantity());
        // sendKafkaMessageAsynchronouslyWithResponse(productId, productCreatedEvent);
        sendKafkaMessageSynchronously(productId, productCreatedEvent);
        return productId;
    }

    private void sendKafkaMessageAsynchronouslyWithResponse(String productId, ProductCreatedEvent productCreatedEvent) {
        CompletableFuture<SendResult<String, ProductCreatedEvent>> response = kafkaTemplate.send("product-created-events-topic", productId, productCreatedEvent);

        response.whenComplete((result, exception) -> {
            if (exception != null) {
                LOGGER.error("Failed to send message", exception);
            } else {
                logKafkaMessageSendSuccess(result);
            }
        });
    }

    private void sendKafkaMessageSynchronously(String productId, ProductCreatedEvent productCreatedEvent) throws ExecutionException, InterruptedException {
        SendResult<String, ProductCreatedEvent> response = kafkaTemplate.send("product-created-events-topic", productId, productCreatedEvent).get();

        logKafkaMessageSendSuccess(response);
    }

    private void logKafkaMessageSendSuccess(SendResult<String, ProductCreatedEvent> response) {
        LOGGER.info("Message sent successfully{}", response.getRecordMetadata());
    }
}
