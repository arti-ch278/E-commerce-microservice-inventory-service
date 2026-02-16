package com.artichourey.ecommerce.inventoryservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.artichourey.ecommerce.inventoryservice.entity.Inventory;

public interface InventoryRepository extends JpaRepository<Inventory,Long>{
	
	Optional<Inventory>findBySkuCode(String skuCode);

}
