package com.BTLJAVA.WebBanThucPhamKho.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressRequest {
    // Các trường này dành cho địa chỉ thực tế
    @NotBlank(message = "Số nhà không được để trống")
    @Size(max = 50)
    private String number;

    @NotBlank(message = "Đường không được để trống")
    @Size(max = 255)
    private String street;

    @NotBlank(message = "Phường/Xã không được để trống")
    @Size(max = 100)
    private String ward;

    @NotBlank(message = "Quận/Huyện không được để trống")
    @Size(max = 100)
    private String district;

    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    @Size(max = 100)
    private String city;
}