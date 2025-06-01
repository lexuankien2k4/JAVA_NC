package com.BTLJAVA.WebBanThucPhamKho.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrderDetailResponse {
    private Integer id;
    private Integer productId;
    private String productName;
    private String productImageUrl; // Thêm để hiển thị
    private Integer quantity;
    private Integer priceAtOrder; // Đơn giá tại thời điểm đặt hàng
    private Integer lineTotal;    // quantity * priceAtOrder
    private String status;
}
