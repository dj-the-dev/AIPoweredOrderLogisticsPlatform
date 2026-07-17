package com.AIPoweredOrder.LogisticsPlatform.cart_service.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<Cart> createCart(@RequestBody CartRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.createCart(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartResponse> getCart(@PathVariable Long id) {
        return ResponseEntity.ok(cartService.getCart(id));
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<CartResponse> getActiveCartByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.getActiveCartByUserId(userId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Cart>> getCartsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.getCartsByUserId(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCart(@PathVariable Long id) {
        cartService.deleteCart(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<Cart> addItem(@PathVariable Long id, @RequestBody CartItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItem(id, request));
    }

    @PutMapping("/{id}/items/{itemId}")
    public ResponseEntity<Cart> updateItemQuantity(@PathVariable Long id, @PathVariable Long itemId,
                                                    @RequestBody CartItemQuantityRequest request) {
        return ResponseEntity.ok(cartService.updateItemQuantity(id, itemId, request));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<Cart> removeItem(@PathVariable Long id, @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(id, itemId));
    }

    @DeleteMapping("/{id}/items")
    public ResponseEntity<Cart> clearCart(@PathVariable Long id) {
        return ResponseEntity.ok(cartService.clearCart(id));
    }

    @PostMapping("/{id}/checkout")
    public ResponseEntity<Cart> checkout(@PathVariable Long id) {
        return ResponseEntity.ok(cartService.checkout(id));
    }
}
