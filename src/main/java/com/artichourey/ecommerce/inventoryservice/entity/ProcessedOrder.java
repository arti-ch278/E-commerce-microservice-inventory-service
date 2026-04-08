package com.artichourey.ecommerce.inventoryservice.entity;

import java.time.LocalDateTime;

import com.artichourey.ecommerce.inventoryservice.enums.OrderInventoryStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "processed_order", uniqueConstraints = @UniqueConstraint(columnNames = "orderId"))
public class ProcessedOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderId;

    private LocalDateTime processedAt;

    // Track step-wise inventory status with default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderInventoryStatus status = OrderInventoryStatus.RESERVED;

    // Optimistic locking to prevent conflicts
    @Version
    @Column(nullable = false)
    private Integer version = 0;

    // Convenience constructor for easy saving
    public ProcessedOrder(String orderId, LocalDateTime processedAt, OrderInventoryStatus status) {
        this.orderId = orderId;
        this.processedAt = processedAt;
        this.status = status;
    }
}