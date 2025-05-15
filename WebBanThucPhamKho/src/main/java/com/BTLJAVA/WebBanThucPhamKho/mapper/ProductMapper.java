package com.BTLJAVA.WebBanThucPhamKho.mapper;

import com.BTLJAVA.WebBanThucPhamKho.dto.response.ProductResponse;
import com.BTLJAVA.WebBanThucPhamKho.entity.Product;
import org.springframework.stereotype.Service;

@Service
public class ProductMapper {
    public ProductResponse toDTO(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .categoryName(product.getCategory().getName())
                .imageUrl(product.getImageUrl())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .soldQuantity(product.getSoldQuantity())
                .expiryDate(product.getExpiryDate())
                .manufactureDate(product.getManufactureDate())
                .build();
    }
}
