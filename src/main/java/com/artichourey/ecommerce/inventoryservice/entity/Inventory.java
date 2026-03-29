package com.artichourey.ecommerce.inventoryservice.entity;

import java.util.HashMap;
import java.util.Map;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MapKeyColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String skuCode;

    @Column(nullable = false)
    private Integer availableQuantity;

    // Map to track reserved quantities per order
    @ElementCollection
    @CollectionTable(name = "inventory_reserved_orders", joinColumns = @JoinColumn(name = "inventory_id"))
    @MapKeyColumn(name = "order_id")
    @Column(name = "reserved_quantity")
    private Map<String, Integer> reservedOrders = new HashMap<>();
}