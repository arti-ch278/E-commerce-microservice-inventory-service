package com.artichourey.ecommerce.inventoryservice.mapper;

import java.util.HashMap;

import org.springframework.stereotype.Component;

import com.artichourey.ecommerce.inventoryservice.dto.InventoryRequest;
import com.artichourey.ecommerce.inventoryservice.dto.InventoryResponse;
import com.artichourey.ecommerce.inventoryservice.entity.Inventory;

@Component
public class InventoryMapper {

    // Convert DTO request to entity
    public Inventory toEntity(InventoryRequest request) {
        Inventory inventory = new Inventory();
        inventory.setSkuCode(request.getSkuCode());
        inventory.setAvailableQuantity(request.getQuantity());
        inventory.setReservedOrders(new HashMap<>()); // initialize reservedOrders map
        return inventory;
    }

    // Convert entity to DTO response
    public InventoryResponse toResponse(Inventory inventory) {
        int available = inventory.getAvailableQuantity() != null ? inventory.getAvailableQuantity() : 0;
        boolean inStock = available > 0;
        return new InventoryResponse(
                inventory.getSkuCode(),
                inStock,
                available
        );
    }
}