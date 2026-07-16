package com.AIPoweredOrder.LogisticsPlatform.inventory_service.warehouse;

import com.AIPoweredOrder.LogisticsPlatform.inventory_service.exception.DuplicateResourceException;
import com.AIPoweredOrder.LogisticsPlatform.inventory_service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    @Transactional
    public Warehouse createWarehouse(WarehouseRequest request) {
        if (warehouseRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Warehouse already exists with code: " + request.code());
        }

        Warehouse warehouse = Warehouse.builder()
                .code(request.code())
                .name(request.name())
                .address(request.address())
                .city(request.city())
                .state(request.state())
                .country(request.country())
                .postalCode(request.postalCode())
                .active(request.active() == null || request.active())
                .build();

        return warehouseRepository.save(warehouse);
    }

    @Transactional
    public Warehouse updateWarehouse(Long id, WarehouseRequest request) {
        Warehouse existing = getWarehouseEntity(id);

        if (request.code() != null && !request.code().equals(existing.getCode())
                && warehouseRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Warehouse already exists with code: " + request.code());
        }

        if (request.code() != null) existing.setCode(request.code());
        if (request.name() != null) existing.setName(request.name());
        if (request.address() != null) existing.setAddress(request.address());
        if (request.city() != null) existing.setCity(request.city());
        if (request.state() != null) existing.setState(request.state());
        if (request.country() != null) existing.setCountry(request.country());
        if (request.postalCode() != null) existing.setPostalCode(request.postalCode());
        if (request.active() != null) existing.setActive(request.active());

        return warehouseRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public Warehouse getWarehouseEntity(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Warehouse> getAllWarehouses() {
        return warehouseRepository.findAll();
    }

    @Transactional
    public void deleteWarehouse(Long id) {
        if (!warehouseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Warehouse not found with id: " + id);
        }
        warehouseRepository.deleteById(id);
    }
}
