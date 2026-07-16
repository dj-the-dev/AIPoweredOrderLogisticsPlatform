package com.AIPoweredOrder.LogisticsPlatform.inventory_service.inventory;

public record InventoryItemRequest(
        Long productId,
        String sku,
        Long warehouseId,
        Integer quantityOnHand,
        Integer reorderLevel,
        Integer reorderQuantity
) {}
