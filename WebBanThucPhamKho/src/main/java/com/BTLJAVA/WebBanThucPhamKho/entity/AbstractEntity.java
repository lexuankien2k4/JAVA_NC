package com.BTLJAVA.WebBanThucPhamKho.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@MappedSuperclass
public abstract class AbstractEntity<T> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private T id;

    @Column(name = "create_at", nullable = false, updatable = false) // Thêm nullable, updatable
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP) // Đảm bảo kiểu dữ liệu DB phù hợp
    private Date createAt;

    @Column(name = "update_at", nullable = false) // Thêm nullable
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP) // Đảm bảo kiểu dữ liệu DB phù hợp
    private Date updateAt;
}
