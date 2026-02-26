package com.artichourey.ecommerce.inventoryservice.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.artichourey.ecommerce.inventoryservice.entity.Inventory;

@DataJpaTest
public class InventoryRepositoryTest {
	
	@Autowired
    private InventoryRepository inventoryRepository;

    @Test
    void findBySkuCode_ShouldReturnInventory() {

        Inventory inventory =
                new Inventory(null, "SKU123", 10);

        inventoryRepository.save(inventory);

        Optional<Inventory> result =
                inventoryRepository.findBySkuCode("SKU123");

        assertTrue(result.isPresent());
        assertEquals(10, result.get().getQuantity());
    }

}
