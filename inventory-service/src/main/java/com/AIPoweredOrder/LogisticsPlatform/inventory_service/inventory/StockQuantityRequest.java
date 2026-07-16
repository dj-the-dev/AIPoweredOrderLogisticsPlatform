package com.AIPoweredOrder.LogisticsPlatform.inventory_service.inventory;

public record StockQuantityRequest(
        int quantity,
        String reason
) {}
