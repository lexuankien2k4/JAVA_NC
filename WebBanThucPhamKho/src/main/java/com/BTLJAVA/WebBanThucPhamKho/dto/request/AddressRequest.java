package com.BTLJAVA.WebBanThucPhamKho.dto.request;

import lombok.Getter;

@Getter
public class AddressRequest {
    private String number;
    private String street;
    private String ward;
    private String district;
    private String city;
}
