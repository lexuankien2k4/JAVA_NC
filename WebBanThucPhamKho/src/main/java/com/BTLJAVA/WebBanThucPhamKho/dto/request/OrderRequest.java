package com.BTLJAVA.WebBanThucPhamKho.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderRequest {
    @NotBlank(message = "Tên người nhận không được để trống")
    private String customerName;

    @NotBlank(message = "Số điện thoại người nhận không được để trống")
    private String customerPhone;

    @Email(message = "Email không hợp lệ")
    private String customerEmail; // Có thể tùy chọn

    @NotNull(message = "Thông tin địa chỉ không được để trống")
    @Valid
    private AddressRequest shippingAddress;

    private String title;

    private String guestCartId;
}
