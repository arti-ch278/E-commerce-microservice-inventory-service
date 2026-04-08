package com.artichourey.ecommerce.inventoryservice.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.artichourey.ecommerce.inventoryservice.entity.Inventory;
import jakarta.persistence.LockModeType;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    // Fetch inventory by SKU (read-only)
    Optional<Inventory> findBySkuCode(String skuCode);

    //Custom query to find inventories that have reserved quantity for a specific order
    @Query("SELECT i FROM Inventory i JOIN i.reservedOrders ro WHERE KEY(ro) = :orderId")
    List<Inventory> findByReservedOrderId(@Param("orderId") String orderId);

    //Atomic stock reservation query (modifies inventory safely)
    @Modifying(clearAutomatically = true) // ensures persistence context is updated
    @Query("UPDATE Inventory i SET i.availableQuantity = i.availableQuantity - :qty " +
           "WHERE i.skuCode = :skuCode AND i.availableQuantity >= :qty")
    int reserveStockAtomic(@Param("skuCode") String skuCode, @Param("qty") int qty);

    //Fetch inventory with PESSIMISTIC WRITE LOCK for safe reservation updates
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.skuCode = :skuCode")
    Optional<Inventory> findBySkuCodeWithLock(@Param("skuCode") String skuCode);
}


