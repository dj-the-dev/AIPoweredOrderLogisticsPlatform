package com.AIPoweredOrder.LogisticsPlatform.product_service.product;

import com.AIPoweredOrder.LogisticsPlatform.product_service.brand.Brand;
import com.AIPoweredOrder.LogisticsPlatform.product_service.brand.BrandRepository;
import com.AIPoweredOrder.LogisticsPlatform.product_service.category.Category;
import com.AIPoweredOrder.LogisticsPlatform.product_service.category.CategoryRepository;
import com.AIPoweredOrder.LogisticsPlatform.product_service.vendor.Vendor;
import com.AIPoweredOrder.LogisticsPlatform.product_service.vendor.VendorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.mapper.Mapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final VendorRepository vendorRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Transactional
    public Product addProduct(ProductRequest request) {
        Vendor vendor = vendorRepository.findById(request.vendorId())
                .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + request.vendorId()));

        Brand brand = request.brandId() != null
                ? brandRepository.findById(request.brandId())
                        .orElseThrow(() -> new RuntimeException("Brand not found with id: " + request.brandId()))
                : null;

        Category category = request.categoryId() != null
                ? categoryRepository.findById(request.categoryId())
                        .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.categoryId()))
                : null;

        Product product = Product.builder()
                .sku(request.sku())
                .name(request.name())
                .description(request.description())
                .shortDescription(request.shortDescription())
                .price(request.price())
                .compareAtPrice(request.compareAtPrice())
                .costPrice(request.costPrice())
                .weight(request.weight())
                .weightUnit(request.weightUnit() != null ? request.weightUnit() : "kg")
                .status(request.status() != null ? request.status() : Product.ProductStatus.DRAFT)
                .metaTitle(request.metaTitle())
                .metaDescription(request.metaDescription())
                .vendor(vendor)
                .brand(brand)
                .category(category)
                .build();

        return productRepository.save(product);
    }

    @Transactional
    @CachePut(value = "products", key = "#id")
    public Product updateProduct(Long id, ProductRequest request) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        if (request.vendorId() != null) {
            existing.setVendor(vendorRepository.findById(request.vendorId())
                    .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + request.vendorId())));
        }
        if (request.brandId() != null) {
            existing.setBrand(brandRepository.findById(request.brandId())
                    .orElseThrow(() -> new RuntimeException("Brand not found with id: " + request.brandId())));
        }
        if (request.categoryId() != null) {
            existing.setCategory(categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.categoryId())));
        }
        if (request.sku() != null) existing.setSku(request.sku());
        if (request.name() != null) existing.setName(request.name());
        if (request.description() != null) existing.setDescription(request.description());
        if (request.shortDescription() != null) existing.setShortDescription(request.shortDescription());
        if (request.price() != null) existing.setPrice(request.price());
        if (request.compareAtPrice() != null) existing.setCompareAtPrice(request.compareAtPrice());
        if (request.costPrice() != null) existing.setCostPrice(request.costPrice());
        if (request.weight() != null) existing.setWeight(request.weight());
        if (request.weightUnit() != null) existing.setWeightUnit(request.weightUnit());
        if (request.status() != null) existing.setStatus(request.status());
        if (request.metaTitle() != null) existing.setMetaTitle(request.metaTitle());
        if (request.metaDescription() != null) existing.setMetaDescription(request.metaDescription());

        return productRepository.save(existing);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public ProductResponseDto getProductById(Long id) {
        Product product= productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        return productMapper.toResponse(product);

    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
}
