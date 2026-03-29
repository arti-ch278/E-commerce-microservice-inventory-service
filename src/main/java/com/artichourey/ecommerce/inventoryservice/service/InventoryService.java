package com.artichourey.ecommerce.inventoryservice.service;

import java.util.List;
import org.springframework.stereotype.Service;

import com.artichourey.ecommerce.events.StockFailedEvent;
import com.artichourey.ecommerce.events.StockReservedEvent;
import com.artichourey.ecommerce.inventoryservice.dto.InventoryRequest;
import com.artichourey.ecommerce.inventoryservice.dto.InventoryResponse;
import com.artichourey.ecommerce.inventoryservice.entity.Inventory;
import com.artichourey.ecommerce.inventoryservice.exception.InventoryNotFoundException;
import com.artichourey.ecommerce.inventoryservice.exception.OutOfStockException;
import com.artichourey.ecommerce.inventoryservice.mapper.InventoryMapper;
import com.artichourey.ecommerce.inventoryservice.producer.InventoryEventProducer;
import com.artichourey.ecommerce.inventoryservice.repository.InventoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {
	
    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    private final InventoryEventProducer inventoryEventProducer;

    // Check inventory availability by SKU 
    public InventoryResponse checkInventory(String skuCode) {
        log.info("Checking inventory for SKU: {}", skuCode);

        Inventory inventory = inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for SKU: " + skuCode));

        int available = inventory.getAvailableQuantity() != null ? inventory.getAvailableQuantity() : 0;
        log.info("Inventory for SKU {}: availableQuantity={}, inStock={}", skuCode, available, available > 0);

        return inventoryMapper.toResponse(inventory);
    }

    // Add new inventory 
    @Transactional
    public void addInventory(InventoryRequest request) {
        log.info("Adding inventory for SKU: {}", request.getSkuCode());
        Inventory inventory = inventoryMapper.toEntity(request);
        inventoryRepository.save(inventory);
        log.info("Inventory added successfully for SKU: {}", request.getSkuCode());
    }

    // Reserve stock for an order 
    @Transactional
    public void reserveStock(String orderId, String skuCode, int quantity) {
        log.info("Reserving {} units of SKU {} for order {}", quantity, skuCode, orderId);

        Inventory inventory = inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for SKU: " + skuCode));

        int available = inventory.getAvailableQuantity() != null ? inventory.getAvailableQuantity() : 0;

        if (available < quantity) {
            log.warn("Insufficient stock for SKU {}. Requested={}, Available={}", available);
            inventoryEventProducer.sendStockFailed(new StockFailedEvent(orderId, "Insufficient stock", skuCode));
            throw new OutOfStockException("Insufficient stock for SKU: " + skuCode);
        }

        // Update reservedOrders map
        inventory.getReservedOrders().put(orderId, quantity);
        inventoryRepository.save(inventory);

        log.info("Stock reserved successfully for order {}. SKU={}, reservedQuantity={}", orderId, skuCode, quantity);
        inventoryEventProducer.sendStockReserved(new StockReservedEvent(orderId, skuCode, quantity, skuCode));
    }

    // Commit stock after payment success 
    @Transactional
    public void commitStock(String orderId) {
        log.info("Committing stock for order {}", orderId);

        List<Inventory> inventories = inventoryRepository.findByReservedOrderId(orderId);

        inventories.forEach(inventory -> {
            Integer reservedQty = inventory.getReservedOrders().get(orderId);
            if (reservedQty != null) {
                int newAvailable = inventory.getAvailableQuantity() - reservedQty;
                inventory.setAvailableQuantity(newAvailable);
                inventory.getReservedOrders().remove(orderId);
                inventoryRepository.save(inventory);
                log.info("Stock committed for SKU {}. Deducted={}, New available={}", inventory.getSkuCode(), reservedQty, newAvailable);
            }
        });
    }

    // Rollback stock after payment failure 
    @Transactional
    public void rollbackStock(String orderId) {
        log.info("Rolling back stock for order {}", orderId);

        List<Inventory> inventories = inventoryRepository.findByReservedOrderId(orderId);

        inventories.forEach(inventory -> {
            Integer reservedQty = inventory.getReservedOrders().get(orderId);
            if (reservedQty != null) {
                // Do not reduce availableQuantity, just remove reservation
                inventory.getReservedOrders().remove(orderId);
                inventoryRepository.save(inventory);
                log.info("Stock rolled back for SKU {}. Released reservedQuantity={}", inventory.getSkuCode(), reservedQty);
            }
        });
    }
}