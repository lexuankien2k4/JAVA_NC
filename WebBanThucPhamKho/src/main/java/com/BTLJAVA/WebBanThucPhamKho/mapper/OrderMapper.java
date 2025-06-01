package com.BTLJAVA.WebBanThucPhamKho.mapper;

import com.BTLJAVA.WebBanThucPhamKho.dto.response.OrderDetailResponse;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.OrderResponse;
import com.BTLJAVA.WebBanThucPhamKho.entity.Order;
import com.BTLJAVA.WebBanThucPhamKho.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final OrderDetailMapper orderDetailMapper;
    private final AddressMapper addressMapper;

    public OrderResponse toDTO(Order order) {
        if (order == null) {
            return null;
        }

        User user = order.getUser();
        Integer userIdValue = (user != null) ? user.getId() : null;
        String userNameValue = (user != null) ? user.getUsername() : null;

        Set<OrderDetailResponse> orderDetailResponses = (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty())
                ? order.getOrderDetails().stream().map(orderDetailMapper::toDTO).collect(Collectors.toSet())
                : Collections.emptySet();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(userIdValue)
                .userName(userNameValue)
                .customerName(order.getCustomerName()) // Lấy từ Order entity
                .customerPhone(order.getCustomerPhone()) // Lấy từ Order entity
                .customerEmail(order.getCustomerEmail()) // Lấy từ Order entity
                .shippingAddress(addressMapper.entityToResponse(order.getAddress())) // AddressMapper không cần context Order nữa
                .title(order.getTitle())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .createdAt(order.getCreateAt()) // Từ AbstractEntity
                .updatedAt(order.getUpdateAt()) // Từ AbstractEntity
                .orderDetails(orderDetailResponses)
                .build();
    }
}