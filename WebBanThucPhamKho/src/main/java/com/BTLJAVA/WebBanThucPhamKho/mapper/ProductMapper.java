
package com.BTLJAVA.WebBanThucPhamKho.mapper;

import com.BTLJAVA.WebBanThucPhamKho.dto.response.ProductResponse;
import com.BTLJAVA.WebBanThucPhamKho.entity.Category;
import com.BTLJAVA.WebBanThucPhamKho.entity.Product;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class ProductMapper {

    public ProductResponse toDTO(Product product) {
        if (product == null) {
            return null;
        }

        Category category = product.getCategory(); // Lấy category ra một biến

        Integer categoryIdValue = null;
        String categoryNameValue = null;

        if (category != null) {
            categoryIdValue = category.getId();
            categoryNameValue = category.getName();
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .categoryName(categoryNameValue) // Sử dụng giá trị đã kiểm tra null
                .categoryId(categoryIdValue)     // Sử dụng giá trị đã kiểm tra null
                .imageUrl(product.getImageUrl())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .soldQuantity(product.getSoldQuantity())
                .expiryDate(product.getExpiryDate())
                .manufactureDate(product.getManufactureDate())
                .build();
    }
}
