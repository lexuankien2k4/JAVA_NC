package com.BTLJAVA.WebBanThucPhamKho.service;

import com.BTLJAVA.WebBanThucPhamKho.dto.request.CartItemRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.request.CartItemUpdateQuantityRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.CartResponse;

public interface CartItemService {
    // UserId có thể là null nếu là guest
    CartResponse addItemToCart(Integer userId, CartItemRequest cartItemRequest);
    CartResponse getCart(Integer userId, String guestCartId); // Nhận cả userId và guestCartId
    CartResponse updateItemQuantity(Integer userId, String guestCartId, Long cartItemId, CartItemUpdateQuantityRequest quantityRequest);
    CartResponse removeItemFromCart(Integer userId, String guestCartId, Long cartItemId);
    CartResponse clearCart(Integer userId, String guestCartId);
    CartResponse mergeCart(Integer userId, String guestCartId); // API mới để hợp nhất giỏ hàng
}
