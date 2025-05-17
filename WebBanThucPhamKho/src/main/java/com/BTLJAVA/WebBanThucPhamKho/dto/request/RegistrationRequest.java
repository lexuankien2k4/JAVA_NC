package com.BTLJAVA.WebBanThucPhamKho.dto.request; // Hoặc package tương ứng trong dự án của bạn

import jakarta.validation.constraints.Email; // Hoặc javax.validation.constraints.Email
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data // Tự động tạo getters, setters, toString, equals, hashCode
@NoArgsConstructor // Constructor không tham số
@AllArgsConstructor // Constructor với tất cả tham số
@Builder // Cho phép sử dụng Builder pattern để tạo đối tượng (tùy chọn)
public class RegistrationRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 50, message = "Tên đăng nhập phải có từ 3 đến 50 ký tự")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 100, message = "Mật khẩu phải có từ 6 đến 100 ký tự")
    // Bạn có thể thêm các ràng buộc phức tạp hơn cho mật khẩu nếu cần (ví dụ: @Pattern)
    private String password;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Định dạng email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự")
    private String fullName;

    // Bạn có thể thêm các trường khác nếu cần, ví dụ:
    // private String phoneNumber;
    // private String address;
}