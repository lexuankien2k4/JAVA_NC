package com.BTLJAVA.WebBanThucPhamKho.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class CartItemResponse {
    private Long id;
    private Integer productId;
    private String productName;
    private String productImageUrl;
    private Integer unitPrice; // Đơn giá của sản phẩm
    private Integer quantity;
    private Integer lineTotal; // Thành tiền: unitPrice * quantity
}
