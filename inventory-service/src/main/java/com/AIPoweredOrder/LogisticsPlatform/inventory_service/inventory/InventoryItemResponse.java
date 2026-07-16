package com.AIPoweredOrder.LogisticsPlatform.inventory_service.inventory;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class InventoryItemResponse implements Serializable {

    private Long id;
    private Long productId;
    private String sku;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private int quantityOnHand;
    private int quantityReserved;
    private int quantityAvailable;
    private int reorderLevel;
    private int reorderQuantity;
    private boolean lowStock;
    private LocalDateTime updatedAt;
}
