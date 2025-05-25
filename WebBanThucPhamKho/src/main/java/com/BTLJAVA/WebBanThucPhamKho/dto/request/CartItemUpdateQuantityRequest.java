package com.BTLJAVA.WebBanThucPhamKho.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemUpdateQuantityRequest {
    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng không thể âm. Để xóa, hãy dùng API xóa.") // Số lượng có thể là 0 để xóa
    private Integer quantity;
}