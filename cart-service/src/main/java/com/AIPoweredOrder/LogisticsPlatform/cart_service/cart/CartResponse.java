package com.AIPoweredOrder.LogisticsPlatform.cart_service.cart;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CartResponse implements Serializable {

    private Long id;
    private Long userId;
    private Cart.CartStatus status;
    private List<CartItemResponse> items;
    private BigDecimal totalAmount;
    private int totalItems;
    private LocalDateTime updatedAt;
}
