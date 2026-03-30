package com.artichourey.ecommerce.inventoryservice.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.artichourey.ecommerce.inventoryservice.entity.ProcessedOrder;

@DataJpaTest
class ProcessedOrderRepositoryTest {

    @Autowired
    private ProcessedOrderRepository repository;

    @Test
    void testExistsByOrderId() {
        repository.save(new ProcessedOrder("ORD123", LocalDateTime.now()));

        boolean exists = repository.existsByOrderId("ORD123");

        assertTrue(exists);
    }
}
