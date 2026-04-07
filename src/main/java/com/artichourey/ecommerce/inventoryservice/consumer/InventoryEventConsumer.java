package com.artichourey.ecommerce.inventoryservice.consumer;

import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.artichourey.ecommerce.events.OrderPlacedEvent;
import com.artichourey.ecommerce.events.PaymentCompletedEvent;
import com.artichourey.ecommerce.events.PaymentFailedEvent;
import com.artichourey.ecommerce.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventConsumer {

    private final InventoryService inventoryService;

    // Handle new order placement
    @KafkaListener(topics = "order-topic", groupId = "inventory-group")
    public void handleOrderPlaced(OrderPlacedEvent event) {

        String traceId = UUID.randomUUID().toString();

        log.info("[{}] Received OrderPlacedEvent | orderId={}, skuCode={}, quantity={}",
                traceId, event.getOrderId(), event.getSkuCode(), event.getQuantity());

        try {
            // Idempotency and reservation handled inside service
            inventoryService.reserveStock(
                    event.getOrderId(),
                    event.getSkuCode(),
                    event.getQuantity()
            );

        } catch (Exception e) {
            log.error("[{}] Error reserving stock | orderId={}, skuCode={}",
                    traceId, event.getOrderId(), event.getSkuCode(), e);

            throw e; // triggers retry and DLQ
        }
    }

    // Handle payment success: commit reserved stock
    @KafkaListener(topics = "payment-success-topic", groupId = "inventory-group")
    public void handlePaymentSuccess(PaymentCompletedEvent event) {

        String traceId = UUID.randomUUID().toString();

        log.info("[{}] Received PaymentCompletedEvent | orderId={}, paymentId={}",
                traceId, event.getOrderId(), event.getPaymentId());

        try {
            // Safe commit, only clears reservation and updates order status
            inventoryService.commitStock(event.getOrderId());

        } catch (Exception e) {
            log.error("[{}] Error committing stock | orderId={}",
                    traceId, event.getOrderId(), e);

            throw e; // triggers retry and DLQ
        }
    }

    // Handle payment failure: rollback stock
    @KafkaListener(topics = "payment-failed-topic", groupId = "inventory-group")
    public void handlePaymentFailed(PaymentFailedEvent event) {

        String traceId = UUID.randomUUID().toString();

        log.info("[{}] Received PaymentFailedEvent | orderId={}, paymentId={}",
                traceId, event.getOrderId(), event.getPaymentId());

        try {
            // Rollback restores stock and clears reservation safely
            inventoryService.rollbackStock(event.getOrderId());

        } catch (Exception e) {
            log.error("[{}] Error rolling back stock | orderId={}",
                    traceId, event.getOrderId(), e);

            throw e; // triggers retry and DLQ
        }
    }
}