package com.BTLJAVA.WebBanThucPhamKho.entity;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "order_details")
public class OrderDetail extends AbstractEntity<Integer> {

    @ManyToOne(fetch = FetchType.LAZY) // LAZY là phù hợp ở đây
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER) // EAGER để dễ lấy thông tin sản phẩm
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false) // Số lượng là bắt buộc
    private Integer quantity;

    @Column(name = "price", nullable = false) // Đơn giá tại thời điểm đặt hàng, bắt buộc
    private Integer price;

    @Column(name = "status", length = 50) // Trạng thái của từng mục
    private String status;
}