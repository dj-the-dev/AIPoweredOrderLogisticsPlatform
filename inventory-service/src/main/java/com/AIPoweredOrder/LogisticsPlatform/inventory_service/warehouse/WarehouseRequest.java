package com.AIPoweredOrder.LogisticsPlatform.inventory_service.warehouse;

public record WarehouseRequest(
        String code,
        String name,
        String address,
        String city,
        String state,
        String country,
        String postalCode,
        Boolean active
) {}
