package com.BTLJAVA.WebBanThucPhamKho.entity;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cart_items", indexes = {
        @Index(name = "idx_guest_cart_id", columnList = "guest_cart_id") // Thêm index cho guest_cart_id
})
public class CartItem extends AbstractEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true) // Cho phép userId là NULL cho guest cart
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false) // Tổng tiền của dòng này (đơn giá * số lượng)
    private Integer price;

    @Column(name = "guest_cart_id", length = 36) // UUID là 36 ký tự
    private String guestCartId; // Dùng để định danh giỏ hàng của khách vãng lai

    // Có thể thêm trường isGuestItem để dễ phân biệt, hoặc dựa vào userId == null
}