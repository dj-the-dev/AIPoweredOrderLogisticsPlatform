package com.AIPoweredOrder.LogisticsPlatform.product_service.vendor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository vendorRepository;

    @Transactional
    public Vendor registerVendor(Vendor vendor) {
        return vendorRepository.save(vendor);
    }

    @Transactional
    public Vendor updateVendor(Long id, Vendor updates) {
        Vendor existing = vendorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + id));

        existing.setName(updates.getName());
        existing.setDescription(updates.getDescription());
        existing.setEmail(updates.getEmail());
        existing.setPhone(updates.getPhone());
        existing.setAddress(updates.getAddress());
        existing.setWebsite(updates.getWebsite());
        existing.setLogoUrl(updates.getLogoUrl());
        existing.setTaxId(updates.getTaxId());
        if (updates.getStatus() != null) {
            existing.setStatus(updates.getStatus());
        }
        return vendorRepository.save(existing);
    }
}
