package com.artichourey.ecommerce.inventoryservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.artichourey.ecommerce.inventoryservice.entity.ProcessedOrder;
import com.artichourey.ecommerce.inventoryservice.enums.OrderInventoryStatus;

public interface ProcessedOrderRepository extends JpaRepository<ProcessedOrder, Long> {

    
    boolean existsByOrderId(String orderId);

    boolean existsByOrderIdAndStatus(String orderId, OrderInventoryStatus status);
  
    Optional<ProcessedOrder> findByOrderId(String orderId);
}

