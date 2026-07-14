package com.AIPoweredOrder.LogisticsPlatform.product_service.product;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "vendor.name", target = "vendorName")
    @Mapping(source = "brand.name", target = "brandName")
    ProductResponseDto toResponse(Product product);
}
