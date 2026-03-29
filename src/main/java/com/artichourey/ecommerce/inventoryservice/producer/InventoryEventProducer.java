package com.artichourey.ecommerce.inventoryservice.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.artichourey.ecommerce.events.StockFailedEvent;
import com.artichourey.ecommerce.events.StockReservedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendStockReserved(StockReservedEvent event) {
        log.info("Sending StockReservedEvent | orderId={}, skuCode={}", event.getOrderId(), event.getSkuCode());
        kafkaTemplate.send("stock-reserved-topic", event.getOrderId(), event);
    }

    public void sendStockFailed(StockFailedEvent event) {
        log.info("Sending StockFailedEvent | orderId={}, reason={}", event.getOrderId(), event.getReason());
        kafkaTemplate.send("stock-failed-topic", event.getOrderId(), event);
    }
}