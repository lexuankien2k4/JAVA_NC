package com.BTLJAVA.WebBanThucPhamKho.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class OrderRequest {
    private List<OrderDetailRequest> orderDetailList;
    private AddressRequest address;
}
