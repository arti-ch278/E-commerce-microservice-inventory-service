package com.artichourey.ecommerce.inventoryservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.artichourey.ecommerce.inventoryservice.dto.InventoryRequest;
import com.artichourey.ecommerce.inventoryservice.dto.InventoryResponse;
import com.artichourey.ecommerce.inventoryservice.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Inventory APIs", description = "Endpoints for checking and managing inventory")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "Check inventory for a SKU")
    @GetMapping("/{skuCode}")
    public ResponseEntity<InventoryResponse> checkInventory(@PathVariable String skuCode) {
        return ResponseEntity.ok(inventoryService.checkInventory(skuCode));
    }

    @Operation(summary = "Add stock for a product", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<Void> addInventory(@Valid @RequestBody InventoryRequest request) {
        inventoryService.addInventory(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}