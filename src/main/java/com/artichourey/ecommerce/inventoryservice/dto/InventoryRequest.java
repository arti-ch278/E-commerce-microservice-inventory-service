package com.artichourey.ecommerce.inventoryservice.dto;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryRequest {
	
	private long id;
	@NotBlank(message="sku code must be not blank")
	private String skuCode;
	@Min(value=0,message="Quantity must be 0 or greater")
	private int quantity;

}
