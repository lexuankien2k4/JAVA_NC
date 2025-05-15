package com.BTLJAVA.WebBanThucPhamKho.entity;

import jakarta.persistence.*;
import lombok.*;
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "addresses")
public class Address extends AbstractEntity<Integer> {

    @Column(name = "number")
    private String number;

    @Column(name = "street")
    private String street;

    @Column(name = "ward")
    private String ward;

    @Column(name = "district")
    private String district;

    @Column(name = "city")
    private String city;

    @OneToOne(mappedBy = "address", fetch = FetchType.EAGER)
    private Order order;

}
