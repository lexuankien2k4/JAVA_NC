package com.BTLJAVA.WebBanThucPhamKho.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

@Setter
@Getter
@Builder
public class OrderResponse {
    private Integer id;
    private Integer userId;
    private String userName;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private AddressResponse shippingAddress;
    private String title;
    private Integer totalPrice;
    private String status;
    private Date createdAt;
    private Date updatedAt;
    private Set<OrderDetailResponse> orderDetails;
}
