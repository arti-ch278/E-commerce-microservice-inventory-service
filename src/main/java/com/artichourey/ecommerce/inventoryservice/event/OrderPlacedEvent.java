package com.artichourey.ecommerce.inventoryservice.event;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderPlacedEvent {

	private String eventId;
	private String orderId;
	private String skuCode;
	private Integer quantity;
	private LocalDateTime eventTime;
	
}
