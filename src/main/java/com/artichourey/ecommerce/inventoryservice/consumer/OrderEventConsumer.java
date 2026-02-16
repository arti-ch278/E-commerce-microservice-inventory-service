package com.artichourey.ecommerce.inventoryservice.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.artichourey.ecommerce.inventoryservice.event.OrderPlacedEvent;
import com.artichourey.ecommerce.inventoryservice.service.InventoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderEventConsumer {

	private final Logger log= LoggerFactory.getLogger(OrderEventConsumer.class);
	private final InventoryService inventoryService;
	
	@KafkaListener(topics="order", groupId="inventory-group")
	
	public void consume(OrderPlacedEvent event) {
		
		log.info("Received order event: {}", event);
        try {
            inventoryService.updateStock(event);
            log.info("Successfully processed order: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to process order: {}", event.getOrderId(), e);
        }
	}
	
	
}
