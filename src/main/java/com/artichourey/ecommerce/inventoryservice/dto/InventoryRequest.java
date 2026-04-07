package com.artichourey.ecommerce.inventoryservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request body for adding or updating inventory")
public class InventoryRequest {

    
//    @Schema(description = "Inventory ID (optional for new inventory)", example = "1")
//    private Long id;

    @NotBlank(message = "sku code must not be blank")
    @Schema(description = "SKU code of the product", example = "SKU12345", required = true)
    private String skuCode;

    @NotNull(message = "Quantity must not be null") 
    @Min(value = 0, message = "Quantity must be 0 or greater")
    @Schema(description = "Quantity of the product in inventory", example = "10", required = true)
    private Integer quantity;
}
