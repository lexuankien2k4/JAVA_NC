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

    @Column(name = "number", length = 50)
    private String number;

    @Column(name = "street", length = 255, nullable = false)
    private String street;

    @Column(name = "ward", length = 100, nullable = false)
    private String ward;

    @Column(name = "district", length = 100, nullable = false)
    private String district;

    @Column(name = "city", length = 100, nullable = false)
    private String city;

}