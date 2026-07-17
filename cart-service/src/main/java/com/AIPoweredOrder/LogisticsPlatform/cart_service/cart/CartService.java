package com.AIPoweredOrder.LogisticsPlatform.cart_service.cart;

import com.AIPoweredOrder.LogisticsPlatform.cart_service.config.CacheNames;
import com.AIPoweredOrder.LogisticsPlatform.cart_service.exception.InvalidCartOperationException;
import com.AIPoweredOrder.LogisticsPlatform.cart_service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public Cart createCart(CartRequest request) {
        return cartRepository.findByUserIdAndStatus(request.userId(), Cart.CartStatus.ACTIVE)
                .orElseGet(() -> cartRepository.save(Cart.builder().userId(request.userId()).build()));
    }

    @Transactional(readOnly = true)
    public Cart getCartEntity(Long id) {
        return cartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + id));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.CARTS, key = "#id")
    public CartResponse getCart(Long id) {
        return toResponse(getCartEntity(id));
    }

    @Transactional(readOnly = true)
    public CartResponse getActiveCartByUserId(Long userId) {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, Cart.CartStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active cart found for user id: " + userId));
        return toResponse(cart);
    }

    @Transactional(readOnly = true)
    public List<Cart> getCartsByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    @Transactional
    @CacheEvict(value = CacheNames.CARTS, key = "#id")
    public void deleteCart(Long id) {
        if (!cartRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cart not found with id: " + id);
        }
        cartRepository.deleteById(id);
    }

    @Transactional
    @CacheEvict(value = CacheNames.CARTS, key = "#id")
    public Cart addItem(Long id, CartItemRequest request) {
        requirePositiveQuantity(request.quantity());
        Cart cart = getCartEntity(id);
        requireActive(cart);

        CartItem item = cartItemRepository.findByCartIdAndProductId(id, request.productId())
                .map(existing -> {
                    existing.setQuantity(existing.getQuantity() + request.quantity());
                    return existing;
                })
                .orElseGet(() -> CartItem.builder()
                        .cart(cart)
                        .productId(request.productId())
                        .sku(request.sku())
                        .productName(request.productName())
                        .unitPrice(request.unitPrice())
                        .quantity(request.quantity())
                        .build());

        cartItemRepository.save(item);
        return getCartEntity(id);
    }

    @Transactional
    @CacheEvict(value = CacheNames.CARTS, key = "#id")
    public Cart updateItemQuantity(Long id, Long itemId, CartItemQuantityRequest request) {
        requirePositiveQuantity(request.quantity());
        Cart cart = getCartEntity(id);
        requireActive(cart);

        CartItem item = findItem(cart, itemId);
        item.setQuantity(request.quantity());
        cartItemRepository.save(item);

        return getCartEntity(id);
    }

    @Transactional
    @CacheEvict(value = CacheNames.CARTS, key = "#id")
    public Cart removeItem(Long id, Long itemId) {
        Cart cart = getCartEntity(id);
        requireActive(cart);

        CartItem item = findItem(cart, itemId);
        cartItemRepository.delete(item);

        return getCartEntity(id);
    }

    @Transactional
    @CacheEvict(value = CacheNames.CARTS, key = "#id")
    public Cart clearCart(Long id) {
        Cart cart = getCartEntity(id);
        requireActive(cart);

        cartItemRepository.deleteAll(cart.getItems());
        return getCartEntity(id);
    }

    @Transactional
    @CacheEvict(value = CacheNames.CARTS, key = "#id")
    public Cart checkout(Long id) {
        Cart cart = getCartEntity(id);
        requireActive(cart);

        if (cart.getItems().isEmpty()) {
            throw new InvalidCartOperationException("Cannot checkout an empty cart, id: " + id);
        }

        cart.setStatus(Cart.CartStatus.CHECKED_OUT);
        return cartRepository.save(cart);
    }

    private CartItem findItem(Cart cart, Long itemId) {
        return cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart item not found with id: " + itemId + " in cart id: " + cart.getId()));
    }

    private void requireActive(Cart cart) {
        if (cart.getStatus() != Cart.CartStatus.ACTIVE) {
            throw new InvalidCartOperationException(
                    "Cart id: " + cart.getId() + " is not active (status: " + cart.getStatus() + ")");
        }
    }

    private void requirePositiveQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }
    }

    private CartResponse toResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setUserId(cart.getUserId());
        response.setStatus(cart.getStatus());
        response.setItems(cart.getItems().stream().map(this::toItemResponse).toList());
        response.setTotalAmount(cart.getItems().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        response.setTotalItems(cart.getItems().stream().mapToInt(CartItem::getQuantity).sum());
        response.setUpdatedAt(cart.getUpdatedAt());
        return response;
    }

    private CartItemResponse toItemResponse(CartItem item) {
        CartItemResponse response = new CartItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProductId());
        response.setSku(item.getSku());
        response.setProductName(item.getProductName());
        response.setUnitPrice(item.getUnitPrice());
        response.setQuantity(item.getQuantity());
        response.setSubtotal(item.getSubtotal());
        return response;
    }
}
