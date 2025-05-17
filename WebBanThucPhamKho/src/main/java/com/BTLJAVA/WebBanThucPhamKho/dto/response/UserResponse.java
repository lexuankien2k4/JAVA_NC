package com.BTLJAVA.WebBanThucPhamKho.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserResponse {
    private Integer id;
    private String userName;
    private String firstName;
    private String lastName;
    private String email;
    private Integer phone;
}
