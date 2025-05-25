package com.BTLJAVA.WebBanThucPhamKho.mapper;
import com.BTLJAVA.WebBanThucPhamKho.dto.request.CategoryRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.CategoryResponse;
import com.BTLJAVA.WebBanThucPhamKho.entity.Category;
import com.BTLJAVA.WebBanThucPhamKho.entity.Product;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Set;

@Component
public class CategoryMapper {

    public CategoryResponse toDTO(Category category) {
        // Xử lý trường hợp đầu vào là null để tránh NullPointerException
        if (category == null) {
            return null;
        }

        // Lấy danh sách sản phẩm từ category
        Set<Product> products = category.getProducts();
        int productCount = 0;
        if (products != null) {
            productCount = products.size();
        }

        // Sử dụng builder của CategoryResponse để tạo đối tượng DTO
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .updatedAt(category.getCreateAt())
                .createdAt(category.getCreateAt())
                .productCount(productCount)
                .build();
    }

//     Nếu bạn cần một phương thức để chuyển từ CategoryRequest (DTO) sang Category (Entity)
//     (Thường dùng khi tạo mới hoặc cập nhật category từ dữ liệu client gửi lên)
     public Category toEntity(CategoryRequest categoryRequest) {
         if (categoryRequest == null) {
             return null;
         }
         Category category = new Category();
         category.setName(categoryRequest.getName());
         return category;
     }
}
