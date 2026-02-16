package com.artichourey.ecommerce.inventoryservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.artichourey.ecommerce.inventoryservice.entity.ProcessedOrder;

public interface ProcessedOrderRepository extends JpaRepository<ProcessedOrder,Long> {

	
	boolean existsByOrderId(String orderId);
	
}
