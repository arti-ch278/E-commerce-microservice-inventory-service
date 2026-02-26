package com.artichourey.ecommerce.inventoryservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.artichourey.ecommerce.inventoryservice.dto.InventoryResponse;
import com.artichourey.ecommerce.inventoryservice.entity.Inventory;
import com.artichourey.ecommerce.inventoryservice.event.OrderPlacedEvent;
import com.artichourey.ecommerce.inventoryservice.mapper.InventoryMapper;
import com.artichourey.ecommerce.inventoryservice.repository.InventoryRepository;
import com.artichourey.ecommerce.inventoryservice.repository.ProcessedOrderRepository;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceImplTest {
	
	@Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryMapper inventoryMapper;

    @Mock
    private ProcessedOrderRepository processedOrderRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void checkInventory_ShouldReturnResponse() {

        Inventory inventory = new Inventory(1L, "SKU123", 5);

        when(inventoryRepository.findBySkuCode("SKU123"))
                .thenReturn(Optional.of(inventory));

        InventoryResponse response =
                inventoryService.checkInventory("SKU123");

        assertTrue(response.isInStock());
        assertEquals(5, response.getAvailableQuantity());
    }

    @Test
    void updateStock_ShouldReduceQuantity() {

        OrderPlacedEvent event = new OrderPlacedEvent();
        event.setOrderId("ORD1");
        event.setSkuCode("SKU123");
        event.setQuantity(2);

        Inventory inventory = new Inventory(1L, "SKU123", 10);

        when(processedOrderRepository.existsByOrderId("ORD1"))
                .thenReturn(false);

        when(inventoryRepository.findBySkuCode("SKU123"))
                .thenReturn(Optional.of(inventory));

        inventoryService.updateStock(event);

        assertEquals(8, inventory.getQuantity());

        verify(inventoryRepository).save(inventory);
        verify(processedOrderRepository).save(any());
    }

    @Test
    void updateStock_ShouldThrowIfInsufficient() {

        OrderPlacedEvent event = new OrderPlacedEvent();
        event.setOrderId("ORD2");
        event.setSkuCode("SKU123");
        event.setQuantity(20);

        Inventory inventory = new Inventory(1L, "SKU123", 5);

        when(processedOrderRepository.existsByOrderId("ORD2"))
                .thenReturn(false);

        when(inventoryRepository.findBySkuCode("SKU123"))
                .thenReturn(Optional.of(inventory));

        assertThrows(RuntimeException.class,
                () -> inventoryService.updateStock(event));
    }
}


