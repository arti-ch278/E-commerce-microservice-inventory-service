package com.artichourey.ecommerce.inventoryservice.mapper;

import java.util.HashMap;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.artichourey.ecommerce.inventoryservice.dto.InventoryRequest;
import com.artichourey.ecommerce.inventoryservice.dto.InventoryResponse;
import com.artichourey.ecommerce.inventoryservice.entity.Inventory;

@Component
public class InventoryMapper {

    public Inventory toEntity(InventoryRequest request) {

        // VALIDATION: prevent negative input
        if (request.getQuantity() != null && request.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        Inventory inventory = new Inventory();

        inventory.setSkuCode(request.getSkuCode());

        // Safe null handling
        inventory.setAvailableQuantity(
                request.getQuantity() != null ? request.getQuantity() : 0
        );

        // IMPORTANT: only initialize if null (future-safe)
        if (inventory.getReservedOrders() == null) {
            inventory.setReservedOrders(new HashMap<>());
        }

        return inventory;
    }

    // Convert entity to DTO response
    public InventoryResponse toResponse(Inventory inventory) {

        int available = Optional
                .ofNullable(inventory.getAvailableQuantity())
                .orElse(0);

        boolean inStock = available > 0;

        return new InventoryResponse(
                inventory.getSkuCode(),
                inStock,
                available
        );
    }
}