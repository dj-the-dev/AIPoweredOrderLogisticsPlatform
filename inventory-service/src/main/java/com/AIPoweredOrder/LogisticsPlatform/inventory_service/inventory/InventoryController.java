package com.AIPoweredOrder.LogisticsPlatform.inventory_service.inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryItem> createInventoryItem(@RequestBody InventoryItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.createInventoryItem(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryItem> updateInventoryItem(@PathVariable Long id,
                                                              @RequestBody InventoryItemRequest request) {
        return ResponseEntity.ok(inventoryService.updateInventoryItem(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryItemResponse> getInventoryItem(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getInventoryItem(id));
    }

    @GetMapping
    public ResponseEntity<List<InventoryItem>> getAllInventoryItems() {
        return ResponseEntity.ok(inventoryService.getAllInventoryItems());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventoryItem(@PathVariable Long id) {
        inventoryService.deleteInventoryItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<InventoryItem>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getByProductId(productId));
    }

    @GetMapping("/product/{productId}/warehouse/{warehouseId}")
    public ResponseEntity<InventoryItem> getByProductAndWarehouse(@PathVariable Long productId,
                                                                   @PathVariable Long warehouseId) {
        return ResponseEntity.ok(inventoryService.getByProductAndWarehouse(productId, warehouseId));
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<InventoryItem>> getByWarehouse(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(inventoryService.getByWarehouse(warehouseId));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryItem>> getLowStockItems() {
        return ResponseEntity.ok(inventoryService.getLowStockItems());
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<Map<String, Object>> checkAvailability(@PathVariable Long id,
                                                                   @RequestParam int quantity) {
        boolean available = inventoryService.checkAvailability(id, quantity);
        return ResponseEntity.ok(Map.of("inventoryItemId", id, "requestedQuantity", quantity, "available", available));
    }

    @GetMapping("/{id}/movements")
    public ResponseEntity<List<StockMovement>> getMovementHistory(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getMovementHistory(id));
    }

    @PostMapping("/{id}/restock")
    public ResponseEntity<InventoryItem> restock(@PathVariable Long id, @RequestBody StockQuantityRequest request) {
        return ResponseEntity.ok(inventoryService.restock(id, request));
    }

    @PostMapping("/{id}/adjust")
    public ResponseEntity<InventoryItem> adjustStock(@PathVariable Long id, @RequestBody StockQuantityRequest request) {
        return ResponseEntity.ok(inventoryService.adjustStock(id, request));
    }

    @PostMapping("/{id}/reserve")
    public ResponseEntity<InventoryItem> reserveStock(@PathVariable Long id, @RequestBody StockReservationRequest request) {
        return ResponseEntity.ok(inventoryService.reserveStock(id, request));
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<InventoryItem> releaseStock(@PathVariable Long id, @RequestBody StockReservationRequest request) {
        return ResponseEntity.ok(inventoryService.releaseStock(id, request));
    }

    @PostMapping("/{id}/deduct")
    public ResponseEntity<InventoryItem> deductStock(@PathVariable Long id, @RequestBody StockReservationRequest request) {
        return ResponseEntity.ok(inventoryService.deductStock(id, request));
    }
}
