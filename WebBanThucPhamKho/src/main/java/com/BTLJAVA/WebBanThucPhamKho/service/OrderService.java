package com.BTLJAVA.WebBanThucPhamKho.service;

import com.BTLJAVA.WebBanThucPhamKho.dto.request.OrderRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {

    OrderResponse placeOrder(Integer userId, OrderRequest orderRequest);

    OrderResponse getOrderByIdForUser(Integer orderId, Integer userId);

    List<OrderResponse> getOrderHistoryForUser(Integer userId);

    OrderResponse getOrderByIdForAdmin(Integer orderId);

    Page<OrderResponse> getAllOrdersForAdmin(Pageable pageable);

    OrderResponse updateOrderStatusForAdmin(Integer orderId, String newStatus);
}