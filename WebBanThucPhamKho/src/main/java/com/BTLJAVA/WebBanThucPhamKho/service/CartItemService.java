package com.BTLJAVA.WebBanThucPhamKho.service;



import com.BTLJAVA.WebBanThucPhamKho.dto.request.CartItemRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.request.CartItemUpdateQuantityRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.CartItemResponse;

import java.util.List;

public interface CartItemService {
    List<CartItemResponse> getCartByUserId(Integer userId);

    CartItemResponse createCartItem(Integer userId, CartItemRequest cartItemRequest);

    CartItemResponse updateCartItem(Integer userId, Integer id, CartItemUpdateQuantityRequest cartItemRequest);

    void deleteCartItem(Integer userId, Integer cartItemId);
}
