package com.AIPoweredOrder.LogisticsPlatform.product_service.product;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
@Data
public class ProductResponseDto implements Serializable {

    private Long id;
    private String name;
    private BigDecimal price;
    private String categoryName;
    private String vendorName;
    private String brandName;
}
