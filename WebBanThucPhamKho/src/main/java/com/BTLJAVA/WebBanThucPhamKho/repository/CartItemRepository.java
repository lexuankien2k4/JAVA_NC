package com.BTLJAVA.WebBanThucPhamKho.repository;

import com.BTLJAVA.WebBanThucPhamKho.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserId(Integer userId);
    List<CartItem> findByGuestCartId(String guestCartId);
    Optional<CartItem> findByUserIdAndProductId(Integer userId, Integer productId);
    Optional<CartItem> findByGuestCartIdAndProductId(String guestCartId, Integer productId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.user.id = :userId")
    void deleteByUserId(Integer userId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.guestCartId = :guestCartId")
    void deleteByGuestCartId(String guestCartId);
}
