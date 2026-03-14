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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Inventory APIs", description = "Endpoints for checking and managing inventory")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inventory")

public class InventoryController {
	
	private final InventoryService inventoryService;
	@Operation(
	        summary = "Check if product is in stock",
	        description = "Returns inventory details for a product by SKU code. Can be public or JWT-protected depending on your policy."
	    )
	    @ApiResponses({
	        @ApiResponse(
	            responseCode = "200",
	            description = "Inventory found",
	            content = @Content(mediaType = "application/json",
	                schema = @Schema(implementation = InventoryResponse.class))
	        ),
	        @ApiResponse(
	            responseCode = "404",
	            description = "Product not found",
	            content = @Content
	        )
	    })
	@GetMapping("/{skuCode}")
	public ResponseEntity<InventoryResponse> isInStock(@PathVariable String skuCode){
		
		return ResponseEntity.ok(inventoryService.checkInventory(skuCode));
		
	}
	
	@Operation(
	        summary = "Add inventory for a product",
	        description = "Adds stock for a product. JWT authentication required.",
	        security = @SecurityRequirement(name = "bearerAuth")
	    )
	    @ApiResponses({
	        @ApiResponse(responseCode = "201", description = "Inventory added successfully", content = @Content),
	        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
	    })
	
	@PostMapping("/")
	public ResponseEntity<String> addInventory(@Valid @RequestBody InventoryRequest inventoryRequest){
		inventoryService.addInventory(inventoryRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body("Inventory added Successfully");
		
	}

}
