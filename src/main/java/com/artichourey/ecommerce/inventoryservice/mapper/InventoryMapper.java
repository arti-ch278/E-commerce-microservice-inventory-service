package com.artichourey.ecommerce.inventoryservice.mapper;

import org.springframework.stereotype.Component;

import com.artichourey.ecommerce.inventoryservice.dto.InventoryRequest;
import com.artichourey.ecommerce.inventoryservice.dto.InventoryResponse;
import com.artichourey.ecommerce.inventoryservice.entity.Inventory;

@Component
public class InventoryMapper {
	
	public Inventory toEntity(InventoryRequest request) {
		Inventory inventory= new Inventory();
		inventory.setSkuCode(request.getSkuCode());
		inventory.setQuantity(request.getQuantity());
	
		return inventory;
		
	}
	public InventoryResponse toResponse(Inventory inventory) {
		
		return new InventoryResponse(
				inventory.getSkuCode(),
				inventory.getQuantity()>0,inventory.getQuantity());
		
		
	}
	

}
