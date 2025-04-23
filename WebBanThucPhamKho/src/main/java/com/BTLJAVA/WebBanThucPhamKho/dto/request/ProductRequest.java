package com.BTLJAVA.WebBanThucPhamKho.dto.request;

import lombok.Getter;

@Getter
public class ProductRequest {
    private String name;
    private String description;
    private String imageUrl;
    private Integer price;
}
