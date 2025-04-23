package com.BTLJAVA.WebBanThucPhamKho.service;


import com.BTLJAVA.WebBanThucPhamKho.dto.request.AddressRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.request.OrderStatusUpdateRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {
    List<OrderResponse> getOrders(Integer pageNumber, Integer pageSize);

    List<OrderResponse> getOrdersByUserId(Integer userId, Integer pageNumber, Integer pageSize);

    OrderResponse createOrder(Integer userId, AddressRequest addressRequest);

    OrderResponse updateStatusOder(Integer id, OrderStatusUpdateRequest orderStatusUpdateRequest);
}
