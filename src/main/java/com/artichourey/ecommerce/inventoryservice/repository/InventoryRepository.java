package com.artichourey.ecommerce.inventoryservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.artichourey.ecommerce.inventoryservice.entity.Inventory;

public interface InventoryRepository extends JpaRepository<Inventory,Long>{
	
	Optional<Inventory>findBySkuCode(String skuCode);
	
	 // Custom query to find inventories that have reserved quantity for a specific order
    @Query("SELECT i FROM Inventory i JOIN i.reservedOrders ro WHERE KEY(ro) = :orderId")
    List<Inventory> findByReservedOrderId(@Param("orderId") String orderId);


}
