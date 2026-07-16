package com.AIPoweredOrder.LogisticsPlatform.inventory_service.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    List<InventoryItem> findByProductId(Long productId);

    Optional<InventoryItem> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    List<InventoryItem> findByWarehouseId(Long warehouseId);

    boolean existsByProductIdAndWarehouseId(Long productId, Long warehouseId);

    @Query("select i from InventoryItem i where (i.quantityOnHand - i.quantityReserved) <= i.reorderLevel")
    List<InventoryItem> findLowStockItems();

    @Query("select i from InventoryItem i where i.warehouse.id = :warehouseId and (i.quantityOnHand - i.quantityReserved) <= i.reorderLevel")
    List<InventoryItem> findLowStockItemsByWarehouse(@Param("warehouseId") Long warehouseId);
}
