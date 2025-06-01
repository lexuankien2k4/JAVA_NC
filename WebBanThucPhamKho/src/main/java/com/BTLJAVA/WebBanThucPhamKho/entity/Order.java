package com.BTLJAVA.WebBanThucPhamKho.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order extends AbstractEntity<Integer> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true) // Đã đúng: cho phép null cho guest
    private User user;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "address_id", referencedColumnName = "id", nullable = false) // Địa chỉ là bắt buộc
    private Address address;

    @Column(name = "title", length = 500)
    private String title;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "customer_phone", nullable = false, length = 20)
    private String customerPhone;

    @Column(name = "customer_email", length = 100) // Email có thể không bắt buộc
    private String customerEmail;

    // FetchType.EAGER và CascadeType.ALL là quan trọng ở đây
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<OrderDetail> orderDetails = new HashSet<>();
}