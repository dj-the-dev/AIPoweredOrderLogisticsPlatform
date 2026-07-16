package com.AIPoweredOrder.LogisticsPlatform.inventory_service.inventory;

public record StockReservationRequest(
        int quantity,
        String referenceId
) {}
