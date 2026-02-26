package com.artichourey.ecommerce.inventoryservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.artichourey.ecommerce.inventoryservice.dto.InventoryRequest;
import com.artichourey.ecommerce.inventoryservice.dto.InventoryResponse;
import com.artichourey.ecommerce.inventoryservice.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(InventoryController.class)
public class InventoryControllerTest {
	
	 @Autowired
	    private MockMvc mockMvc;

	    @MockBean
	    private InventoryService inventoryService;

	    @Autowired
	    private ObjectMapper objectMapper;

	    @Test
	    void isInStock_ShouldReturnInventoryResponse() throws Exception {

	        InventoryResponse response =
	                new InventoryResponse("SKU123", true, 10);

	        when(inventoryService.checkInventory("SKU123"))
	                .thenReturn(response);

	        mockMvc.perform(get("/api/inventory/SKU123"))
	                .andExpect(status().isOk())
	                .andExpect(jsonPath("$.skuCode").value("SKU123"))
	                .andExpect(jsonPath("$.inStock").value(true))
	                .andExpect(jsonPath("$.availableQuantity").value(10));
	    }

	    @Test
	    void addInventory_ShouldReturnCreated() throws Exception {

	        InventoryRequest request =
	                new InventoryRequest(1L, "SKU123", 20);

	        doNothing().when(inventoryService).addInventory(any());

	        mockMvc.perform(post("/api/inventory/")
	                .contentType(MediaType.APPLICATION_JSON)
	                .content(objectMapper.writeValueAsString(request)))
	                .andExpect(status().isCreated());
	    }
	}