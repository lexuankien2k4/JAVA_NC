package com.BTLJAVA.WebBanThucPhamKho.repository;


import com.BTLJAVA.WebBanThucPhamKho.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    Page<Product> findByNameContainingIgnoreCase(String nameKeyword, Pageable pageable);
}
