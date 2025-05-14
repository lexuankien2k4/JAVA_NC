package com.BTLJAVA.WebBanThucPhamKho.mapper;


import com.BTLJAVA.WebBanThucPhamKho.dto.response.CartItemResponse;
import com.BTLJAVA.WebBanThucPhamKho.entity.CartItem;
import org.springframework.stereotype.Service;

@Service
public class CartItemMapper {
    public CartItemResponse toDTO(CartItem cartItem) {
        return CartItemResponse.builder()
                .id(cartItem.getId())
                .productId(cartItem.getProduct().getId())
                .name(cartItem.getProduct().getName())
                .quantity(cartItem.getQuantity())
                .price(cartItem.getPrice())
                .build();
    }
}
