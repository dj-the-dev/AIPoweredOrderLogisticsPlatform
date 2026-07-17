package com.AIPoweredOrder.LogisticsPlatform.cart_service.cart;

import java.math.BigDecimal;

public record CartItemRequest(
        Long productId,
        String sku,
        String productName,
        BigDecimal unitPrice,
        Integer quantity
) {}
