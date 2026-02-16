package com.artichourey.ecommerce.inventoryservice.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.artichourey.ecommerce.inventoryservice.dto.InventoryRequest;
import com.artichourey.ecommerce.inventoryservice.dto.InventoryResponse;
import com.artichourey.ecommerce.inventoryservice.entity.Inventory;
import com.artichourey.ecommerce.inventoryservice.entity.ProcessedOrder;
import com.artichourey.ecommerce.inventoryservice.event.OrderPlacedEvent;
import com.artichourey.ecommerce.inventoryservice.exception.InventoryNotFoundException;
import com.artichourey.ecommerce.inventoryservice.mapper.InventoryMapper;
import com.artichourey.ecommerce.inventoryservice.repository.InventoryRepository;
import com.artichourey.ecommerce.inventoryservice.repository.ProcessedOrderRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {
	
	private final Logger log= LoggerFactory.getLogger(InventoryService.class);

	private final InventoryRepository inventoryRepository;
	private final InventoryMapper inventoryMapper;
	private final ProcessedOrderRepository processedOrderRepository;
	private boolean inStock;
	
	public InventoryResponse checkInventory(String skuCode) {
		 log.info("Checking inventory for SKU: {}", skuCode);
		
		Inventory inventory=inventoryRepository.findBySkuCode(skuCode).orElseThrow(()->new InventoryNotFoundException
				("Inventory not found exception"+skuCode));
		

	    boolean inStock = inventory.getQuantity() > 0;
	    log.info("Inventory for SKU {}: quantity={}, inStock={}", skuCode, inventory.getQuantity(), inStock);

	    return new InventoryResponse(
	            inventory.getSkuCode(),
	            inStock,
	            inventory.getQuantity()
	    );	
	}
	
	
	public void addInventory(InventoryRequest inventoryRequest) {
		log.info("Adding new inventory: {}", inventoryRequest);
		Inventory inventory=inventoryMapper.toEntity(inventoryRequest);
		Inventory saved=inventoryRepository.save(inventory);
		log.info("Saved inventory: {}", saved);
		
	}
	@Transactional
	public void updateStock(OrderPlacedEvent event) {
		 log.info("Processing order: {}", event);
		if(processedOrderRepository.existsByOrderId(event.getOrderId())) {
			 log.warn("Order {} already processed. Skipping.", event.getOrderId());
			return;
		}
		Inventory inventory=inventoryRepository.findBySkuCode(event.getSkuCode()).orElseThrow(()->new InventoryNotFoundException(event.getSkuCode()));
	Integer availableQuantity=inventory.getQuantity();
	Integer orderedQuantity=event.getQuantity();
	
	log.info("Inventory before update: SKU={}, availableQuantity={}, orderedQuantity={}",
              event.getSkuCode(), availableQuantity, orderedQuantity);
	if(availableQuantity<orderedQuantity) {
		log.error("Insufficient stock for SKU {}", event.getSkuCode());
		throw new RuntimeException("Insuffient stock for this skuCode");
	}
	inventory.setQuantity(availableQuantity-orderedQuantity);
	inventoryRepository.save(inventory);
	log.info("Inventory updated: SKU={}, newQuantity={}", event.getSkuCode(), inventory.getQuantity());
	ProcessedOrder processed=new ProcessedOrder();
	processed.setOrderId(event.getOrderId());
	processed.setProcessedAt(LocalDateTime.now());
	processedOrderRepository.save(processed);
	log.info("Marked order {} as processed at {}", event.getOrderId(), processed.getProcessedAt());
	}
	
}
