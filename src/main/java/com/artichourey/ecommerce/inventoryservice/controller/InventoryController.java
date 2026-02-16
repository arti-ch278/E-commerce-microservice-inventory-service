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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inventory")

public class InventoryController {
	
	private final InventoryService inventoryService;
	
	@GetMapping("/{skuCode}")
	public ResponseEntity<InventoryResponse> isInStock(@PathVariable String skuCode){
		
		return ResponseEntity.ok(inventoryService.checkInventory(skuCode));
		
	}
	@PostMapping("/")
	public ResponseEntity<String> addInventory(@Valid @RequestBody InventoryRequest inventoryRequest){
		inventoryService.addInventory(inventoryRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body("Inventory added Successfully");
		
	}

}
