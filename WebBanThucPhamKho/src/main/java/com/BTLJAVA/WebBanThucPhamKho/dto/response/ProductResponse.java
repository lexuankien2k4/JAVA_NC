package com.BTLJAVA.WebBanThucPhamKho.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor; // Thêm để Builder hoạt động tốt với các trường mới
import lombok.NoArgsConstructor;  // Thêm để Builder hoạt động tốt với các trường mới

import java.util.Date;

@Setter
@Getter
@Builder
@AllArgsConstructor // Đảm bảo Builder có thể truy cập constructor đầy đủ
@NoArgsConstructor  // Cần thiết cho một số trường hợp sử dụng và JPA/frameworks
public class ProductResponse {
    private Integer id;
    private String name;
    private String categoryName;
    private Integer categoryId; // <<--- THÊM TRƯỜNG NÀY
    private String imageUrl;
    private Integer price;
    private Integer stockQuantity;
    private Integer soldQuantity;
    private Date manufactureDate;
    private Date expiryDate;
}
