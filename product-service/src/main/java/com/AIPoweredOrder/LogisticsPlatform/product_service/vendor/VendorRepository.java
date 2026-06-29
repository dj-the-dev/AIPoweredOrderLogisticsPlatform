package com.AIPoweredOrder.LogisticsPlatform.product_service.vendor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {
    boolean existsByEmail(String email);
}
