package com.artichourey.ecommerce.inventoryservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.artichourey.ecommerce.events.StockFailedEvent;
import com.artichourey.ecommerce.events.StockReservedEvent;
import com.artichourey.ecommerce.inventoryservice.dto.InventoryRequest;
import com.artichourey.ecommerce.inventoryservice.dto.InventoryResponse;
import com.artichourey.ecommerce.inventoryservice.entity.Inventory;
import com.artichourey.ecommerce.inventoryservice.entity.ProcessedOrder;
import com.artichourey.ecommerce.inventoryservice.enums.OrderInventoryStatus;
import com.artichourey.ecommerce.inventoryservice.exception.InventoryNotFoundException;
import com.artichourey.ecommerce.inventoryservice.exception.OutOfStockException;
import com.artichourey.ecommerce.inventoryservice.mapper.InventoryMapper;
import com.artichourey.ecommerce.inventoryservice.producer.InventoryEventProducer;
import com.artichourey.ecommerce.inventoryservice.repository.InventoryRepository;
import com.artichourey.ecommerce.inventoryservice.repository.ProcessedOrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;
    private final ProcessedOrderRepository processedOrderRepository;
    private final InventoryEventProducer inventoryEventProducer;

    // Check inventory availability
    public InventoryResponse checkInventory(String skuCode) {
        log.info("Checking inventory for SKU: {}", skuCode);

        Inventory inventory = inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for SKU: " + skuCode));

        int available = Optional.ofNullable(inventory.getAvailableQuantity()).orElse(0);
        log.info("Inventory for SKU {}: availableQuantity={}, inStock={}", skuCode, available, available > 0);

        return inventoryMapper.toResponse(inventory);
    }

    //  Add new inventory
    @Transactional
    public void addInventory(InventoryRequest request) {
        log.info("Adding inventory for SKU: {}", request.getSkuCode());
        Inventory inventory = inventoryMapper.toEntity(request);
        inventoryRepository.save(inventory);
        log.info("Inventory added successfully for SKU: {}", request.getSkuCode());
    }

    // Reserve stock: atomic update + pessimistic lock + idempotency
    @Transactional
    public void reserveStock(String orderId, String skuCode, int quantity) {
        log.info("Reserving {} units of SKU {} for order {}", quantity, skuCode, orderId);

        // Idempotency: skip if already RESERVED
        if (processedOrderRepository.existsByOrderIdAndStatus(orderId, OrderInventoryStatus.RESERVED)) {
            log.warn("Order {} already RESERVED, skipping", orderId);
            return;
        }

        // Atomic DB update to prevent negative stock
        int updatedRows = inventoryRepository.reserveStockAtomic(skuCode, quantity);
        if (updatedRows == 0) {
            log.warn("Stock reservation failed for SKU {}. Requested={}", skuCode, quantity);
            inventoryEventProducer.sendStockFailed(new StockFailedEvent(orderId, "Insufficient stock", skuCode));
            throw new OutOfStockException("Insufficient stock for SKU: " + skuCode);
        }

        //  Fetch inventory with PESSIMISTIC WRITE LOCK to safely update reservedOrders
        Inventory inventory = inventoryRepository.findBySkuCodeWithLock(skuCode)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found"));

        // Store reservation in reservedOrders map
        inventory.getReservedOrders().put(orderId, quantity);
        inventoryRepository.save(inventory);

        // Save processed order with status RESERVED
        processedOrderRepository.save(new ProcessedOrder(orderId, LocalDateTime.now(), OrderInventoryStatus.RESERVED));

        log.info("Stock reserved successfully for order {}. SKU={}, reservedQuantity={}", orderId, skuCode, quantity);

        inventoryEventProducer.sendStockReserved(new StockReservedEvent(orderId, skuCode, quantity, skuCode));
    }

    // Commit stock: clear reservation, mark order COMMITTED
    @Transactional
    public void commitStock(String orderId) {
        log.info("Committing stock for order {}", orderId);

        Optional<ProcessedOrder> processedOrderOpt = processedOrderRepository.findByOrderId(orderId);
        if (processedOrderOpt.isEmpty()) {
            log.warn("ProcessedOrder not found for order {}. Skipping commit.", orderId);
            return;
        }

        ProcessedOrder processedOrder = processedOrderOpt.get();

        if (processedOrder.getStatus() == OrderInventoryStatus.COMMITTED) {
            log.warn("Order {} already COMMITTED, skipping", orderId);
            return;
        }

        List<Inventory> inventories = inventoryRepository.findByReservedOrderId(orderId);
        inventories.forEach(inventory -> {
            Integer reservedQty = inventory.getReservedOrders().get(orderId);
            if (reservedQty != null) {
                inventory.getReservedOrders().remove(orderId);
                inventoryRepository.save(inventory);
                log.info("Stock committed for SKU {}. Reservation cleared={}", inventory.getSkuCode(), reservedQty);
            }
        });

        // Update order status to COMMITTED
        processedOrder.setStatus(OrderInventoryStatus.COMMITTED);
        processedOrderRepository.save(processedOrder);
    }

    //  Rollback stock: restore available quantity, clear reservation, mark order ROLLED_BACK
    @Transactional
    public void rollbackStock(String orderId) {
        log.info("Rolling back stock for order {}", orderId);

        Optional<ProcessedOrder> processedOrderOpt = processedOrderRepository.findByOrderId(orderId);
        if (processedOrderOpt.isEmpty()) {
            log.warn("ProcessedOrder not found for order {}. Skipping rollback.", orderId);
            return;
        }

        ProcessedOrder processedOrder = processedOrderOpt.get();

        if (processedOrder.getStatus() == OrderInventoryStatus.ROLLED_BACK) {
            log.warn("Order {} already ROLLED_BACK, skipping", orderId);
            return;
        }

        List<Inventory> inventories = inventoryRepository.findByReservedOrderId(orderId);
        inventories.forEach(inventory -> {
            Integer reservedQty = inventory.getReservedOrders().get(orderId);
            if (reservedQty != null) {
                // Restore stock safely
                inventory.setAvailableQuantity(Optional.ofNullable(inventory.getAvailableQuantity()).orElse(0) + reservedQty);
                // Remove reservation
                inventory.getReservedOrders().remove(orderId);
                inventoryRepository.save(inventory);
                log.info("Stock rolled back for SKU {}. Restored quantity={}", inventory.getSkuCode(), reservedQty);
            }
        });

        // Update order status to ROLLED_BACK
        processedOrder.setStatus(OrderInventoryStatus.ROLLED_BACK);
        processedOrderRepository.save(processedOrder);
    }
}