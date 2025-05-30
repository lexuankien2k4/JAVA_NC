package com.BTLJAVA.WebBanThucPhamKho.repository;

import com.BTLJAVA.WebBanThucPhamKho.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    Page<Product> findByNameContainingIgnoreCase(String nameKeyword, Pageable pageable);
}
