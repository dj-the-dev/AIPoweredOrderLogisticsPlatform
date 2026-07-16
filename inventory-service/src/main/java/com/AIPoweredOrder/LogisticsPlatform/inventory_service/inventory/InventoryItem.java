package com.AIPoweredOrder.LogisticsPlatform.inventory_service.inventory;

import com.AIPoweredOrder.LogisticsPlatform.inventory_service.warehouse.Warehouse;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_items", indexes = {
        @Index(name = "idx_inventory_product", columnList = "product_id"),
        @Index(name = "idx_inventory_sku", columnList = "sku"),
        @Index(name = "idx_inventory_warehouse", columnList = "warehouse_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_inventory_product_warehouse", columnNames = {"product_id", "warehouse_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false, length = 100)
    private String sku;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "quantity_on_hand", nullable = false)
    @Builder.Default
    private int quantityOnHand = 0;

    @Column(name = "quantity_reserved", nullable = false)
    @Builder.Default
    private int quantityReserved = 0;

    @Column(name = "reorder_level", nullable = false)
    @Builder.Default
    private int reorderLevel = 0;

    @Column(name = "reorder_quantity", nullable = false)
    @Builder.Default
    private int reorderQuantity = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public int getQuantityAvailable() {
        return quantityOnHand - quantityReserved;
    }

    public boolean isLowStock() {
        return getQuantityAvailable() <= reorderLevel;
    }
}
