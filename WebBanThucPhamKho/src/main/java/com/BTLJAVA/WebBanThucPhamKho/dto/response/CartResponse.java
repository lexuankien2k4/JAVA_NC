package com.BTLJAVA.WebBanThucPhamKho.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Builder
public class CartResponse {
    private List<CartItemResponse> items;
    private int totalUniqueItems;
    private int totalQuantity;
    private Integer subtotalPrice;
    private String guestCartId; // <<--- THÊM TRƯỜNG NÀY
}