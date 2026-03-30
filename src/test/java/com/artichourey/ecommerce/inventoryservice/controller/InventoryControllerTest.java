package com.artichourey.ecommerce.inventoryservice.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.artichourey.ecommerce.inventoryservice.dto.InventoryResponse;
import com.artichourey.ecommerce.inventoryservice.service.InventoryService;

@WebMvcTest(InventoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @Test
    void testCheckInventory() throws Exception {

        when(inventoryService.checkInventory("SKU123"))
                .thenReturn(new InventoryResponse("SKU123", true, 10));

        mockMvc.perform(get("/api/inventory/SKU123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inStock").value(true));
    }

    @Test
    void testAddInventory() throws Exception {

        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skuCode\":\"SKU123\",\"quantity\":10}"))
                .andExpect(status().isCreated());
    }
}