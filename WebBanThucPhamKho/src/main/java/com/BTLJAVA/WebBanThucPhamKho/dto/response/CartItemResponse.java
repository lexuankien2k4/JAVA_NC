package com.BTLJAVA.WebBanThucPhamKho.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class CartItemResponse {
    private Integer id;
    private Integer productId;
    private String name;
    private Integer quantity;
    private Integer price;
}
