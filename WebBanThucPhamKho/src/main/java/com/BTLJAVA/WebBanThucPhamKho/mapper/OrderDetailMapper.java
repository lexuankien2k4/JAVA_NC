package com.BTLJAVA.WebBanThucPhamKho.mapper;

import com.BTLJAVA.WebBanThucPhamKho.dto.response.OrderDetailResponse;
import com.BTLJAVA.WebBanThucPhamKho.entity.OrderDetail;
import com.BTLJAVA.WebBanThucPhamKho.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class OrderDetailMapper {

    public OrderDetailResponse toDTO(OrderDetail orderDetail) {
        if (orderDetail == null) {
            return null;
        }

        Product product = orderDetail.getProduct();
        Integer productIdValue = null;
        String productNameValue = null;
        String productImageUrlValue = null;

        if (product != null) {
            productIdValue = product.getId();
            productNameValue = product.getName();
            productImageUrlValue = product.getImageUrl(); // Giả sử Product entity có getImageUrl()
        }

        Integer priceAtOrderValue = orderDetail.getPrice(); // Lấy đơn giá đã lưu trong OrderDetail
        Integer quantityValue = orderDetail.getQuantity();
        Integer lineTotalValue = 0;

        if (priceAtOrderValue != null && quantityValue != null) {
            lineTotalValue = priceAtOrderValue * quantityValue;
        }

        return OrderDetailResponse.builder()
                .id(orderDetail.getId())
                .productId(productIdValue)
                .productName(productNameValue)
                .productImageUrl(productImageUrlValue)
                .quantity(quantityValue)
                .priceAtOrder(priceAtOrderValue)
                .lineTotal(lineTotalValue)
                .status(orderDetail.getStatus())
                .build();
    }

    // Thông thường, bạn không cần phương thức toEntity cho OrderDetail một cách độc lập
    // vì OrderDetail được tạo và quản lý thông qua Order.
    // Tuy nhiên, nếu có use case cụ thể, bạn có thể thêm:
    /*
    public OrderDetail requestToEntity(SomeOrderDetailRequest request, Order order, Product product) {
        if (request == null) return null;
        return OrderDetail.builder()
                .order(order)
                .product(product)
                .quantity(request.getQuantity())
                .price(request.getPriceAtOrder()) // Hoặc lấy từ product.getPrice() nếu là tạo mới
                .status(request.getStatus())
                .build();
    }
    */
}