package com.BTLJAVA.WebBanThucPhamKho.dto.request;

import lombok.Getter;

import java.util.Date;

@Getter
public class ProductRequest {
    private String name;
    private Integer categoryId;
    private String imageUrl;
    private Integer price;
    private Integer stockQuantity;
    private Integer soldQuantity;
    private Date manufactureDate;
    private Date expiryDate;
}
