package com.BTLJAVA.WebBanThucPhamKho.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@Builder
public class CategoryResponse {
    private Integer id;
    private String name;
    private Date createdAt;
    private Date updatedAt;
    private int productCount; // Số lượng sản phẩm trong danh mục này
}
