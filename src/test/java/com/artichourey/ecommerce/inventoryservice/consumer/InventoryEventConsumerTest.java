package com.artichourey.ecommerce.inventoryservice.consumer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.artichourey.ecommerce.events.OrderPlacedEvent;
import com.artichourey.ecommerce.events.PaymentCompletedEvent;
import com.artichourey.ecommerce.events.PaymentFailedEvent;
import com.artichourey.ecommerce.inventoryservice.service.InventoryService;

@ExtendWith(MockitoExtension.class)
class InventoryEventConsumerTest {

    @InjectMocks
    private InventoryEventConsumer consumer;

    @Mock
    private InventoryService inventoryService;

    @Test
    void testHandleOrderPlaced_Success() {
        // Updated constructor with all fields
        OrderPlacedEvent event = new OrderPlacedEvent("EVT1","ORD1","SKU123",2,LocalDateTime.now(),"USER1");

        consumer.handleOrderPlaced(event);

        verify(inventoryService)
                .reserveStock("ORD1", "SKU123", 2);
    }

    @Test
    void testHandleOrderPlaced_Exception() {
        OrderPlacedEvent event = new OrderPlacedEvent(
                "EVT1", "ORD1", "SKU123", 2, LocalDateTime.now(), "USER1"
        );

        doThrow(new RuntimeException("DB error"))
                .when(inventoryService)
                .reserveStock(any(), any(), anyInt());

        assertThrows(RuntimeException.class,
                () -> consumer.handleOrderPlaced(event));

        verify(inventoryService)
                .reserveStock("ORD1", "SKU123", 2);
    }

    @Test
    void testHandlePaymentSuccess() {
        PaymentCompletedEvent event = new PaymentCompletedEvent(
                "EVT2",            
                "ORD1",            
                "PAY123",          
                "SUCCESS",         
                "USER1",           
                LocalDateTime.now()
        );

        consumer.handlePaymentSuccess(event);

        verify(inventoryService).commitStock("ORD1");
    }

    @Test
    void testHandlePaymentFailed() {
        PaymentFailedEvent event = new PaymentFailedEvent(
                "EVT3",            
                "ORD1",            
                "PAY123",          
                "FAILED",          
                "USER1",           
                LocalDateTime.now() 
        );

        consumer.handlePaymentFailed(event);

        verify(inventoryService).rollbackStock("ORD1");
    }
}