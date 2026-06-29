package com.AIPoweredOrder.LogisticsPlatform.product_service.product;

import java.math.BigDecimal;

public record ProductRequest(
        String sku,
        String name,
        String description,
        String shortDescription,
        BigDecimal price,
        BigDecimal compareAtPrice,
        BigDecimal costPrice,
        BigDecimal weight,
        String weightUnit,
        Product.ProductStatus status,
        String metaTitle,
        String metaDescription,
        Long vendorId,
        Long brandId,
        Long categoryId
) {}
