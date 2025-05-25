package com.BTLJAVA.WebBanThucPhamKho.repository;

import com.BTLJAVA.WebBanThucPhamKho.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // Cho người dùng đã đăng nhập
    Optional<CartItem> findByUserIdAndProductId(Integer userId, Integer productId);
    List<CartItem> findByUserId(Integer userId);
    void deleteByUserId(Integer userId); // Để xóa giỏ hàng của user

    // Cho khách vãng lai
    Optional<CartItem> findByGuestCartIdAndProductId(String guestCartId, Integer productId);
    List<CartItem> findByGuestCartId(String guestCartId);
    void deleteByGuestCartId(String guestCartId); // Để xóa giỏ hàng của guest sau khi merge
}
