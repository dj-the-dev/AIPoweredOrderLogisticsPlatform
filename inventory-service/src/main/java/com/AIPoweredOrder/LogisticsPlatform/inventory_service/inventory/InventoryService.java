package com.AIPoweredOrder.LogisticsPlatform.inventory_service.inventory;

import com.AIPoweredOrder.LogisticsPlatform.inventory_service.config.CacheNames;
import com.AIPoweredOrder.LogisticsPlatform.inventory_service.exception.DuplicateResourceException;
import com.AIPoweredOrder.LogisticsPlatform.inventory_service.exception.InsufficientStockException;
import com.AIPoweredOrder.LogisticsPlatform.inventory_service.exception.ResourceNotFoundException;
import com.AIPoweredOrder.LogisticsPlatform.inventory_service.warehouse.Warehouse;
import com.AIPoweredOrder.LogisticsPlatform.inventory_service.warehouse.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final StockMovementRepository stockMovementRepository;
    private final WarehouseService warehouseService;

    @Transactional
    public InventoryItem createInventoryItem(InventoryItemRequest request) {
        Warehouse warehouse = warehouseService.getWarehouseEntity(request.warehouseId());

        if (inventoryItemRepository.existsByProductIdAndWarehouseId(request.productId(), request.warehouseId())) {
            throw new DuplicateResourceException("Inventory item already exists for product id "
                    + request.productId() + " in warehouse id " + request.warehouseId());
        }

        int initialQuantity = request.quantityOnHand() != null ? request.quantityOnHand() : 0;
        if (initialQuantity < 0) {
            throw new IllegalArgumentException("quantityOnHand cannot be negative");
        }

        InventoryItem item = InventoryItem.builder()
                .productId(request.productId())
                .sku(request.sku())
                .warehouse(warehouse)
                .quantityOnHand(initialQuantity)
                .reorderLevel(request.reorderLevel() != null ? request.reorderLevel() : 0)
                .reorderQuantity(request.reorderQuantity() != null ? request.reorderQuantity() : 0)
                .build();

        item = inventoryItemRepository.save(item);

        if (initialQuantity > 0) {
            recordMovement(item, StockMovement.MovementType.STOCK_IN, initialQuantity, null, "Initial stock");
        }

        return item;
    }

    @Transactional
    @CacheEvict(value = CacheNames.INVENTORY_ITEMS, key = "#id")
    public InventoryItem updateInventoryItem(Long id, InventoryItemRequest request) {
        InventoryItem existing = getInventoryItemEntity(id);

        if (request.sku() != null) existing.setSku(request.sku());
        if (request.reorderLevel() != null) existing.setReorderLevel(request.reorderLevel());
        if (request.reorderQuantity() != null) existing.setReorderQuantity(request.reorderQuantity());
        if (request.warehouseId() != null) {
            existing.setWarehouse(warehouseService.getWarehouseEntity(request.warehouseId()));
        }

        return inventoryItemRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public InventoryItem getInventoryItemEntity(Long id) {
        return inventoryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with id: " + id));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.INVENTORY_ITEMS, key = "#id")
    public InventoryItemResponse getInventoryItem(Long id) {
        return toResponse(getInventoryItemEntity(id));
    }

    @Transactional(readOnly = true)
    public List<InventoryItem> getByProductId(Long productId) {
        return inventoryItemRepository.findByProductId(productId);
    }

    @Transactional(readOnly = true)
    public InventoryItem getByProductAndWarehouse(Long productId, Long warehouseId) {
        return inventoryItemRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory item not found for product id " + productId + " in warehouse id " + warehouseId));
    }

    @Transactional(readOnly = true)
    public List<InventoryItem> getByWarehouse(Long warehouseId) {
        return inventoryItemRepository.findByWarehouseId(warehouseId);
    }

    @Transactional(readOnly = true)
    public List<InventoryItem> getAllInventoryItems() {
        return inventoryItemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<InventoryItem> getLowStockItems() {
        return inventoryItemRepository.findLowStockItems();
    }

    @Transactional
    @CacheEvict(value = CacheNames.INVENTORY_ITEMS, key = "#id")
    public void deleteInventoryItem(Long id) {
        if (!inventoryItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Inventory item not found with id: " + id);
        }
        inventoryItemRepository.deleteById(id);
    }

    @Transactional
    @CacheEvict(value = CacheNames.INVENTORY_ITEMS, key = "#id")
    public InventoryItem restock(Long id, StockQuantityRequest request) {
        requirePositiveQuantity(request.quantity());
        InventoryItem item = getInventoryItemEntity(id);

        item.setQuantityOnHand(item.getQuantityOnHand() + request.quantity());
        item = inventoryItemRepository.save(item);

        recordMovement(item, StockMovement.MovementType.STOCK_IN, request.quantity(), null, request.reason());
        return item;
    }

    @Transactional
    @CacheEvict(value = CacheNames.INVENTORY_ITEMS, key = "#id")
    public InventoryItem adjustStock(Long id, StockQuantityRequest request) {
        InventoryItem item = getInventoryItemEntity(id);

        int newQuantityOnHand = item.getQuantityOnHand() + request.quantity();
        if (newQuantityOnHand < item.getQuantityReserved()) {
            throw new InsufficientStockException(
                    "Adjustment would bring on-hand quantity below reserved quantity for inventory item id: " + id);
        }

        item.setQuantityOnHand(newQuantityOnHand);
        item = inventoryItemRepository.save(item);

        recordMovement(item, StockMovement.MovementType.ADJUSTMENT, request.quantity(), null, request.reason());
        return item;
    }

    @Transactional
    @CacheEvict(value = CacheNames.INVENTORY_ITEMS, key = "#id")
    public InventoryItem reserveStock(Long id, StockReservationRequest request) {
        requirePositiveQuantity(request.quantity());
        InventoryItem item = getInventoryItemEntity(id);

        if (item.getQuantityAvailable() < request.quantity()) {
            throw new InsufficientStockException("Insufficient available stock for inventory item id: " + id
                    + " (requested " + request.quantity() + ", available " + item.getQuantityAvailable() + ")");
        }

        item.setQuantityReserved(item.getQuantityReserved() + request.quantity());
        item = inventoryItemRepository.save(item);

        recordMovement(item, StockMovement.MovementType.RESERVED, request.quantity(), request.referenceId(), null);
        return item;
    }

    @Transactional
    @CacheEvict(value = CacheNames.INVENTORY_ITEMS, key = "#id")
    public InventoryItem releaseStock(Long id, StockReservationRequest request) {
        requirePositiveQuantity(request.quantity());
        InventoryItem item = getInventoryItemEntity(id);

        if (item.getQuantityReserved() < request.quantity()) {
            throw new InsufficientStockException("Cannot release more stock than is reserved for inventory item id: " + id);
        }

        item.setQuantityReserved(item.getQuantityReserved() - request.quantity());
        item = inventoryItemRepository.save(item);

        recordMovement(item, StockMovement.MovementType.RELEASED, request.quantity(), request.referenceId(), null);
        return item;
    }

    @Transactional
    @CacheEvict(value = CacheNames.INVENTORY_ITEMS, key = "#id")
    public InventoryItem deductStock(Long id, StockReservationRequest request) {
        requirePositiveQuantity(request.quantity());
        InventoryItem item = getInventoryItemEntity(id);

        if (item.getQuantityReserved() < request.quantity()) {
            throw new InsufficientStockException("Cannot deduct more stock than is reserved for inventory item id: " + id);
        }

        item.setQuantityReserved(item.getQuantityReserved() - request.quantity());
        item.setQuantityOnHand(item.getQuantityOnHand() - request.quantity());
        item = inventoryItemRepository.save(item);

        recordMovement(item, StockMovement.MovementType.DEDUCTED, request.quantity(), request.referenceId(), null);
        return item;
    }

    @Transactional(readOnly = true)
    public boolean checkAvailability(Long id, int quantity) {
        InventoryItem item = getInventoryItemEntity(id);
        return item.getQuantityAvailable() >= quantity;
    }

    @Transactional(readOnly = true)
    public List<StockMovement> getMovementHistory(Long id) {
        getInventoryItemEntity(id);
        return stockMovementRepository.findByInventoryItemIdOrderByCreatedAtDesc(id);
    }

    private void recordMovement(InventoryItem item, StockMovement.MovementType type, int quantity,
                                 String referenceId, String reason) {
        StockMovement movement = StockMovement.builder()
                .inventoryItem(item)
                .type(type)
                .quantity(quantity)
                .referenceId(referenceId)
                .reason(reason)
                .build();
        stockMovementRepository.save(movement);
    }

    private void requirePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }
    }

    private InventoryItemResponse toResponse(InventoryItem item) {
        InventoryItemResponse response = new InventoryItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProductId());
        response.setSku(item.getSku());
        response.setWarehouseId(item.getWarehouse().getId());
        response.setWarehouseCode(item.getWarehouse().getCode());
        response.setWarehouseName(item.getWarehouse().getName());
        response.setQuantityOnHand(item.getQuantityOnHand());
        response.setQuantityReserved(item.getQuantityReserved());
        response.setQuantityAvailable(item.getQuantityAvailable());
        response.setReorderLevel(item.getReorderLevel());
        response.setReorderQuantity(item.getReorderQuantity());
        response.setLowStock(item.isLowStock());
        response.setUpdatedAt(item.getUpdatedAt());
        return response;
    }
}
