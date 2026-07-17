package com.AIPoweredOrder.LogisticsPlatform.cart_service.cart;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CartItemResponse implements Serializable {

    private Long id;
    private Long productId;
    private String sku;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal subtotal;
}
