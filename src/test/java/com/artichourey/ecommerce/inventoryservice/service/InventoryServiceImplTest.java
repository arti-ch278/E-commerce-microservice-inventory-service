package com.artichourey.ecommerce.inventoryservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.artichourey.ecommerce.inventoryservice.dto.InventoryResponse;
import com.artichourey.ecommerce.inventoryservice.entity.Inventory;
import com.artichourey.ecommerce.inventoryservice.entity.ProcessedOrder;
import com.artichourey.ecommerce.inventoryservice.enums.OrderInventoryStatus;
import com.artichourey.ecommerce.inventoryservice.exception.OutOfStockException;
import com.artichourey.ecommerce.inventoryservice.mapper.InventoryMapper;
import com.artichourey.ecommerce.inventoryservice.producer.InventoryEventProducer;
import com.artichourey.ecommerce.inventoryservice.repository.InventoryRepository;
import com.artichourey.ecommerce.inventoryservice.repository.ProcessedOrderRepository;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @InjectMocks
    private InventoryService inventoryService;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryEventProducer inventoryEventProducer;

    @Mock
    private InventoryMapper inventoryMapper;

    @Mock
    private ProcessedOrderRepository processedOrderRepository;

    private Inventory inventory;

    @BeforeEach
    void setup() {
        inventory = new Inventory();
        inventory.setSkuCode("SKU123");
        inventory.setAvailableQuantity(10);
        inventory.setReservedOrders(new HashMap<>());
    }

    @Test
    void testCheckInventory() {
        when(inventoryRepository.findBySkuCode("SKU123")).thenReturn(Optional.of(inventory));
        when(inventoryMapper.toResponse(inventory))
                .thenReturn(new InventoryResponse("SKU123", true, 10));

        InventoryResponse response = inventoryService.checkInventory("SKU123");

        assertTrue(response.isInStock());
    }

 // SUCCESS CASE
    @Test
    void testReserveStock_Success() {

        when(processedOrderRepository.existsByOrderIdAndStatus(
                "ORD1", OrderInventoryStatus.RESERVED))
                .thenReturn(false);

        when(inventoryRepository.reserveStockAtomic("SKU123", 5))
                .thenReturn(1);

        when(inventoryRepository.findBySkuCodeWithLock("SKU123"))
                .thenReturn(Optional.of(inventory));

        inventoryService.reserveStock("ORD1", "SKU123", 5);

        assertEquals(5, inventory.getReservedOrders().get("ORD1"));

        verify(inventoryRepository).save(inventory);
        verify(processedOrderRepository).save(any());
        verify(inventoryEventProducer).sendStockReserved(any());
    }

    // ALREADY PROCESSED
    @Test
    void testReserveStock_AlreadyProcessed() {

        when(processedOrderRepository.existsByOrderIdAndStatus(
                "ORD1", OrderInventoryStatus.RESERVED))
                .thenReturn(true);

        inventoryService.reserveStock("ORD1", "SKU123", 5);

        verify(inventoryRepository, never()).reserveStockAtomic(any(), anyInt());
        verify(inventoryRepository, never()).save(any());
        verify(inventoryEventProducer, never()).sendStockReserved(any());
    }

    // INSUFFICIENT STOCK
    @Test
    void testReserveStock_InsufficientStock() {

        when(processedOrderRepository.existsByOrderIdAndStatus(
                "ORD1", OrderInventoryStatus.RESERVED))
                .thenReturn(false);

        when(inventoryRepository.reserveStockAtomic("SKU123", 5))
                .thenReturn(0); // simulate failure

        assertThrows(OutOfStockException.class,
                () -> inventoryService.reserveStock("ORD1", "SKU123", 5));

        verify(inventoryEventProducer).sendStockFailed(any());
    }

    // COMMIT STOCK
    @Test
    void testCommitStock() {

        inventory.getReservedOrders().put("ORD1", 5);

        ProcessedOrder processedOrder =
                new ProcessedOrder("ORD1", LocalDateTime.now(), OrderInventoryStatus.RESERVED);

        when(processedOrderRepository.findByOrderId("ORD1"))
                .thenReturn(Optional.of(processedOrder));

        when(inventoryRepository.findByReservedOrderId("ORD1"))
                .thenReturn(List.of(inventory));

        inventoryService.commitStock("ORD1");

        assertFalse(inventory.getReservedOrders().containsKey("ORD1"));

        verify(inventoryRepository).save(inventory);
        verify(processedOrderRepository).save(processedOrder);
    }

    // ROLLBACK STOCK
    @Test
    void testRollbackStock() {

        inventory.setAvailableQuantity(10);
        inventory.getReservedOrders().put("ORD1", 5);

        ProcessedOrder processedOrder =
                new ProcessedOrder("ORD1", LocalDateTime.now(), OrderInventoryStatus.RESERVED);

        when(processedOrderRepository.findByOrderId("ORD1"))
                .thenReturn(Optional.of(processedOrder));

        when(inventoryRepository.findByReservedOrderId("ORD1"))
                .thenReturn(List.of(inventory));

        inventoryService.rollbackStock("ORD1");

        assertFalse(inventory.getReservedOrders().containsKey("ORD1"));
        assertEquals(15, inventory.getAvailableQuantity()); // restored

        verify(inventoryRepository).save(inventory);
        verify(processedOrderRepository).save(processedOrder);
    }
}