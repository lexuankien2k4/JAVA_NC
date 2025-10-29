package com.BTLJAVA.WebBanThucPhamKho.service.impl;

import com.BTLJAVA.WebBanThucPhamKho.dto.request.CartItemRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.request.CartItemUpdateQuantityRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.CartItemResponse;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.CartResponse;
import com.BTLJAVA.WebBanThucPhamKho.entity.CartItem;
import com.BTLJAVA.WebBanThucPhamKho.entity.Product;
import com.BTLJAVA.WebBanThucPhamKho.entity.User;
import com.BTLJAVA.WebBanThucPhamKho.mapper.CartItemMapper; // Đảm bảo bạn có CartItemMapper
import com.BTLJAVA.WebBanThucPhamKho.repository.CartItemRepository; // Đảm bảo bạn có CartItemRepository
import com.BTLJAVA.WebBanThucPhamKho.repository.ProductRepository;
import com.BTLJAVA.WebBanThucPhamKho.repository.UserRepository;
import com.BTLJAVA.WebBanThucPhamKho.service.CartItemService; // Giữ tên interface service của bạn
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartItemMapper cartItemMapper;

    @Override
    @Transactional
    public CartResponse addItemToCart(Integer userId, CartItemRequest cartItemRequest) {
        log.info("SERVICE - addItemToCart - UserID: {}, GuestCartID: {}, ProductID: {}",
                userId, cartItemRequest.getGuestCartId(), cartItemRequest.getProductId());

        Product product = productRepository.findById(cartItemRequest.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm với ID: " + cartItemRequest.getProductId()));

        User userEntity = (userId != null) ? userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId)) : null;

        String guestCartIdToUse = determineGuestCartId(userEntity, cartItemRequest.getGuestCartId());

        Optional<CartItem> existingCartItemOpt = findCartItem(userId, guestCartIdToUse, product.getId());

        CartItem cartItemToSave;
        int requestedQuantity = cartItemRequest.getQuantity();

        if (existingCartItemOpt.isPresent()) {
            // Cập nhật item đã có
            cartItemToSave = existingCartItemOpt.get();
            int newQuantity = cartItemToSave.getQuantity() + requestedQuantity;
            checkStock(product, newQuantity); // Sử dụng helper
            cartItemToSave.setQuantity(newQuantity);
            cartItemToSave.setPrice(product.getPrice() * newQuantity);
        } else {
            // Tạo item mới
            checkStock(product, requestedQuantity); // Sử dụng helper
            cartItemToSave = CartItem.builder()
                    .user(userEntity)
                    .product(product)
                    .quantity(requestedQuantity)
                    .price(product.getPrice() * requestedQuantity)
                    .guestCartId(userEntity == null ? guestCartIdToUse : null)
                    .build();
        }
        cartItemRepository.save(cartItemToSave);
        return getCart(userId, guestCartIdToUse);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(Integer userId, String guestCartId) {
        List<CartItem> cartItems = findCartItems(userId, guestCartId); // Sử dụng helper
        String currentGuestCartId = (userId == null) ? guestCartId : null;

        if (cartItems.isEmpty()) {
            return CartResponse.builder().items(Collections.emptyList()).totalUniqueItems(0).totalQuantity(0).subtotalPrice(0).guestCartId(currentGuestCartId).build();
        }

        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(cartItemMapper::toDTO) // Đơn giản hóa, bỏ try-catch
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        int totalQuantity = itemResponses.stream().mapToInt(CartItemResponse::getQuantity).sum();
        int subtotalPrice = itemResponses.stream().mapToInt(item -> item.getLineTotal() != null ? item.getLineTotal() : 0).sum();

        return CartResponse.builder()
                .items(itemResponses)
                .totalUniqueItems(itemResponses.size())
                .totalQuantity(totalQuantity)
                .subtotalPrice(subtotalPrice)
                .guestCartId(currentGuestCartId)
                .build();
    }


    @Override
    @Transactional
    public CartResponse updateItemQuantity(Integer userId, String guestCartId, Long cartItemId, CartItemUpdateQuantityRequest quantityRequest) {
        log.info("SERVICE - updateItemQuantity - CartItemID: {}, NewQty: {}", cartItemId, quantityRequest.getQuantity());

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy mặt hàng trong giỏ với ID: " + cartItemId));

        validateCartItemOwnership(cartItem, userId, guestCartId); // Sử dụng helper

        if (quantityRequest.getQuantity() <= 0) {
            cartItemRepository.delete(cartItem);
            log.info("SERVICE - CartItemID: {} removed.", cartItemId);
        } else {
            Product product = Optional.ofNullable(cartItem.getProduct())
                    .orElseThrow(() -> new EntityNotFoundException("Sản phẩm liên kết với mặt hàng trong giỏ không tồn tại."));

            checkStock(product, quantityRequest.getQuantity()); // Sử dụng helper

            cartItem.setQuantity(quantityRequest.getQuantity());
            cartItem.setPrice(product.getPrice() * quantityRequest.getQuantity());
            cartItemRepository.save(cartItem);
            log.info("SERVICE - CartItemID: {} quantity updated.", cartItemId);
        }
        return getCart(userId, guestCartId);
    }

    @Override
    @Transactional
    public CartResponse removeItemFromCart(Integer userId, String guestCartId, Long cartItemId) {
        log.info("SERVICE - Remove CartItemID: {}", cartItemId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy mặt hàng trong giỏ với ID: " + cartItemId));

        validateCartItemOwnership(cartItem, userId, guestCartId); // Sử dụng helper

        cartItemRepository.delete(cartItem);
        log.info("SERVICE - CartItemID: {} removed.", cartItemId);
        return getCart(userId, guestCartId);
    }

    @Override
    @Transactional
    public CartResponse clearCart(Integer userId, String guestCartId) {
        if (userId != null) {
            log.info("SERVICE - Clearing cart for UserID: {}", userId);
            cartItemRepository.deleteByUserId(userId);
        } else if (guestCartId != null && !guestCartId.trim().isEmpty()) {
            log.info("SERVICE - Clearing cart for GuestCartID: {}", guestCartId);
            cartItemRepository.deleteByGuestCartId(guestCartId);
        } else {
            log.warn("SERVICE - clearCart called without userId or guestCartId.");
        }
        return getCart(userId, guestCartId);
    }

    @Override
    @Transactional
    public CartResponse mergeCart(Integer userId, String guestCartId) {
        log.info("SERVICE - Merging GuestCartID: {} into UserID: {}", guestCartId, userId);
        if (userId == null) {
            throw new IllegalArgumentException("UserID là bắt buộc để hợp nhất giỏ hàng.");
        }
        if (guestCartId == null || guestCartId.trim().isEmpty()) {
            return getCart(userId, null); // Không có gì để hợp nhất
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        List<CartItem> guestCartItems = cartItemRepository.findByGuestCartId(guestCartId);
        if (guestCartItems.isEmpty()) {
            return getCart(userId, null); // Giỏ hàng khách trống
        }

        for (CartItem guestItem : guestCartItems) {
            Product product = guestItem.getProduct();
            if (product == null) {
                log.warn("SERVICE - Merge: GuestItem ID {} có product null. Đang xóa.", guestItem.getId());
                cartItemRepository.delete(guestItem);
                continue;
            }

            Optional<CartItem> userCartItemOpt = cartItemRepository.findByUserIdAndProductId(userId, product.getId());

            if (userCartItemOpt.isPresent()) {
                // ĐÃ CÓ: Cộng dồn số lượng
                CartItem userItem = userCartItemOpt.get();
                int newQuantity = userItem.getQuantity() + guestItem.getQuantity();
                int stock = getStock(product); // Dùng helper

                if (stock < newQuantity) {
                    log.warn("SERVICE - Merge: Không đủ kho cho Product ID {}. Đặt về số lượng tối đa.", product.getId());
                    newQuantity = stock;
                }
                userItem.setQuantity(newQuantity);
                userItem.setPrice(product.getPrice() * newQuantity);
                cartItemRepository.save(userItem);
                cartItemRepository.delete(guestItem); // Xóa item của guest
            } else {
                // CHƯA CÓ: Chuyển item của guest cho user
                int stock = getStock(product); // Dùng helper
                if (stock < guestItem.getQuantity()) {
                    guestItem.setQuantity(stock);
                    guestItem.setPrice(product.getPrice() * stock);
                }

                if (guestItem.getQuantity() > 0) {
                    guestItem.setUser(user);
                    guestItem.setGuestCartId(null); // Rất quan trọng
                    cartItemRepository.save(guestItem);
                } else {
                    cartItemRepository.delete(guestItem); // Xóa nếu hết hàng
                }
            }
        }
        return getCart(userId, null);
    }

    // --- CÁC HÀM HELPER GOM LOGIC LẶP LẠI ---

    /**
     * Kiểm tra số lượng tồn kho. Ném RuntimeException nếu không đủ.
     */
    private void checkStock(Product product, int requestedQuantity) {
        int stock = getStock(product); // Dùng helper lấy stock
        if (stock < requestedQuantity) {
            log.warn("SERVICE - checkStock - Không đủ kho cho Product ID {}. Yêu cầu: {}, Tồn: {}",
                    product.getId(), requestedQuantity, stock);
            throw new RuntimeException("Số lượng sản phẩm " + product.getName() + " không đủ trong kho.");
        }
    }

    /**
     * Lấy số lượng tồn kho, xử lý trường hợp null.
     */
    private int getStock(Product product) {
        return (product.getStockQuantity() != null) ? product.getStockQuantity() : 0;
    }

    /**
     * Xác thực xem user/guest có quyền sở hữu cartItem này không.
     */
    private void validateCartItemOwnership(CartItem cartItem, Integer userId, String guestCartId) {
        if (userId != null) { // Người dùng đã đăng nhập
            if (cartItem.getUser() == null || !cartItem.getUser().getId().equals(userId)) {
                log.warn("SERVICE - ValidateOwnership - UserID: {} cố truy cập CartItemID: {} không thuộc về mình.", userId, cartItem.getId());
                throw new SecurityException("Bạn không có quyền cập nhật mặt hàng này (user mismatch).");
            }
        } else if (guestCartId != null) { // Khách vãng lai
            if (cartItem.getGuestCartId() == null || !cartItem.getGuestCartId().equals(guestCartId)) {
                log.warn("SERVICE - ValidateOwnership - GuestCartID: {} cố truy cập CartItemID: {} không thuộc về mình.", guestCartId, cartItem.getId());
                throw new SecurityException("Bạn không có quyền cập nhật mặt hàng này (guest cart ID mismatch).");
            }
        } else { // Không có danh tính
            log.warn("SERVICE - ValidateOwnership - Cố gắng truy cập CartItemID: {} mà không có userId hoặc guestCartId.", cartItem.getId());
            throw new SecurityException("Không thể xác định giỏ hàng để cập nhật.");
        }
    }

    /**
     * Tìm các CartItem dựa trên danh tính (user hoặc guest).
     */
    private List<CartItem> findCartItems(Integer userId, String guestCartId) {
        if (userId != null) {
            return cartItemRepository.findByUserId(userId);
        } else if (guestCartId != null && !guestCartId.trim().isEmpty()) {
            return cartItemRepository.findByGuestCartId(guestCartId);
        }
        return Collections.emptyList();
    }

    /**
     * Tìm một CartItem cụ thể dựa trên danh tính (user hoặc guest) và ProductId.
     */
    private Optional<CartItem> findCartItem(Integer userId, String guestCartId, Integer productId) {
        if (userId != null) {
            return cartItemRepository.findByUserIdAndProductId(userId, productId);
        } else if (guestCartId != null && !guestCartId.trim().isEmpty()) {
            return cartItemRepository.findByGuestCartIdAndProductId(guestCartId, productId);
        }
        return Optional.empty();
    }

    /**
     * Quyết định guestCartId sẽ được sử dụng, tạo mới nếu cần.
     */
    private String determineGuestCartId(User user, String requestGuestCartId) {
        boolean isGuest = (user == null);
        String guestCartIdToUse = requestGuestCartId;

        if (isGuest && (requestGuestCartId == null || requestGuestCartId.trim().isEmpty())) {
            guestCartIdToUse = UUID.randomUUID().toString();
            log.info("SERVICE - determineGuestCartId - Tạo guest cart ID mới: {}", guestCartIdToUse);
        }
        return guestCartIdToUse;
    }
}