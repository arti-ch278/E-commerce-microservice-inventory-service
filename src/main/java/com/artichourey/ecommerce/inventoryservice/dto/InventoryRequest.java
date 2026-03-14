package com.artichourey.ecommerce.inventoryservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request body for adding or updating inventory")
public class InventoryRequest {
	
	@Schema(description = "Inventory ID (optional for new inventory)", example = "1")
	private long id;
	
	@NotBlank(message="sku code must be not blank")
	@Schema(description = "SKU code of the product", example = "SKU12345", required = true)
	private String skuCode;
	
	@Min(value=0,message="Quantity must be 0 or greater")
	@Schema(description = "Quantity of the product in inventory", example = "10", required = true)
	private int quantity;

}
