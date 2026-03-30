package com.artichourey.ecommerce.inventoryservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    void testReserveStock_Success() {

        when(processedOrderRepository.existsByOrderId("ORD1")).thenReturn(false);
        when(inventoryRepository.findBySkuCode("SKU123")).thenReturn(Optional.of(inventory));

        inventoryService.reserveStock("ORD1", "SKU123", 5);

        assertEquals(5, inventory.getReservedOrders().get("ORD1"));

        verify(inventoryRepository).save(inventory);
        verify(processedOrderRepository).save(any());
        verify(inventoryEventProducer).sendStockReserved(any());
    }

    @Test
    void testReserveStock_AlreadyProcessed() {

        when(processedOrderRepository.existsByOrderId("ORD1")).thenReturn(true);

        inventoryService.reserveStock("ORD1", "SKU123", 5);

        verify(inventoryRepository, times(0)).save(any());
        verify(inventoryEventProducer, times(0)).sendStockReserved(any());
    }

    @Test
    void testReserveStock_InsufficientStock() {

        inventory.setAvailableQuantity(2);

        when(processedOrderRepository.existsByOrderId("ORD1")).thenReturn(false);
        when(inventoryRepository.findBySkuCode("SKU123")).thenReturn(Optional.of(inventory));

        assertThrows(OutOfStockException.class,
                () -> inventoryService.reserveStock("ORD1", "SKU123", 5));

        verify(inventoryEventProducer).sendStockFailed(any());
    }

    @Test
    void testCommitStock() {

        inventory.getReservedOrders().put("ORD1", 5);

        when(inventoryRepository.findByReservedOrderId("ORD1"))
                .thenReturn(List.of(inventory));

        inventoryService.commitStock("ORD1");

        assertEquals(5, inventory.getAvailableQuantity());
        assertFalse(inventory.getReservedOrders().containsKey("ORD1"));
    }

    @Test
    void testRollbackStock() {

        inventory.getReservedOrders().put("ORD1", 5);

        when(inventoryRepository.findByReservedOrderId("ORD1"))
                .thenReturn(List.of(inventory));

        inventoryService.rollbackStock("ORD1");

        assertFalse(inventory.getReservedOrders().containsKey("ORD1"));
    }
}