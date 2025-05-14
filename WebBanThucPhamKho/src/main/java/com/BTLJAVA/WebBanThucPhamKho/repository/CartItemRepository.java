package com.BTLJAVA.WebBanThucPhamKho.repository;


import com.BTLJAVA.WebBanThucPhamKho.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByUserId(Integer userId);
}
