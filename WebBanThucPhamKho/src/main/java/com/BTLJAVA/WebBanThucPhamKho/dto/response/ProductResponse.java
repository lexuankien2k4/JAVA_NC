package com.BTLJAVA.WebBanThucPhamKho.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@Builder
public class ProductResponse {
    private Integer id;
    private String name;
    private String categoryName;      // Trả về tên danh mục thay vì object Category
    private String imageUrl;
    private Integer price;
    private Integer stockQuantity;
    private Integer soldQuantity;
    private Date manufactureDate;   // ISO format: yyyy-MM-dd
    private Date expiryDate;
}
