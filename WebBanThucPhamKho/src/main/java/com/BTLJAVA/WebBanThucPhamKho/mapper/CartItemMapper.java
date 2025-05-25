package com.BTLJAVA.WebBanThucPhamKho.mapper;


import com.BTLJAVA.WebBanThucPhamKho.dto.response.CartItemResponse;
import com.BTLJAVA.WebBanThucPhamKho.entity.CartItem;
import com.BTLJAVA.WebBanThucPhamKho.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class CartItemMapper {

    public CartItemResponse toDTO(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }

        Product product = cartItem.getProduct(); // Lấy product ra một biến để dễ kiểm tra và tái sử dụng

        Integer unitPrice = null;
        String productName = null;
        String productImageUrl = null;
        Integer productId = null;

        if (product != null) {
            unitPrice = product.getPrice(); // Đây là đơn giá của sản phẩm
            productName = product.getName();
            productImageUrl = product.getImageUrl();
            productId = product.getId();
        }

        // cartItem.getPrice() trong entity của bạn đang lưu tổng tiền của dòng (line total)
        // dựa trên logic trong CartServiceImpl: cartItem.setPrice(product.getPrice() * newQuantity);
        Integer lineTotal = cartItem.getPrice();

        return CartItemResponse.builder()
                .id(cartItem.getId()) // ID của chính CartItem
                .productId(productId)
                .productName(productName)
                .productImageUrl(productImageUrl)
                .unitPrice(unitPrice) // Đơn giá của sản phẩm
                .quantity(cartItem.getQuantity())
                .lineTotal(lineTotal) // Tổng tiền của dòng này
                .build();
    }

}