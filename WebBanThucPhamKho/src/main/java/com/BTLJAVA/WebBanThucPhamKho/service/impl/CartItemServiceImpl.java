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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartItemMapper cartItemMapper; // Đảm bảo bạn có CartItemMapper

    @Override
    @Transactional
    public CartResponse addItemToCart(Integer userId, CartItemRequest cartItemRequest) {
        log.info("SERVICE - addItemToCart - UserID: {}, GuestCartID in request: {}, ProductID: {}, Quantity: {}",
                userId, cartItemRequest.getGuestCartId(), cartItemRequest.getProductId(), cartItemRequest.getQuantity());

        Product product = productRepository.findById(cartItemRequest.getProductId())
                .orElseThrow(() -> {
                    log.error("SERVICE - addItemToCart - Product not found with ID: {}", cartItemRequest.getProductId());
                    return new EntityNotFoundException("Không tìm thấy sản phẩm với ID: " + cartItemRequest.getProductId());
                });
        log.debug("SERVICE - addItemToCart - Product found: {}", product.getName());

        User userEntity = null;
        if (userId != null) {
            userEntity = userRepository.findById(userId) // User entity có thể là LAZY
                    .orElseThrow(() -> {
                        log.error("SERVICE - addItemToCart - User not found with ID: {}", userId);
                        return new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId);
                    });
            log.debug("SERVICE - addItemToCart - User found: {}", userEntity.getUsername());
        }

        String guestCartIdToUse = cartItemRequest.getGuestCartId();
        boolean isGuest = (userEntity == null);

        if (isGuest && (guestCartIdToUse == null || guestCartIdToUse.trim().isEmpty())) {
            guestCartIdToUse = UUID.randomUUID().toString();
            log.info("SERVICE - addItemToCart - New guest cart created with ID: {}", guestCartIdToUse);
        }
        log.debug("SERVICE - addItemToCart - Effective GuestCartID for operation: {}", guestCartIdToUse);

        Optional<CartItem> existingCartItemOpt;
        if (isGuest) {
            existingCartItemOpt = cartItemRepository.findByGuestCartIdAndProductId(guestCartIdToUse, product.getId());
        } else {
            existingCartItemOpt = cartItemRepository.findByUserIdAndProductId(userId, product.getId());
        }

        CartItem cartItemToSave;
        int requestedQuantity = cartItemRequest.getQuantity();

        if (existingCartItemOpt.isPresent()) {
            cartItemToSave = existingCartItemOpt.get();
            log.info("SERVICE - addItemToCart - Product ID {} already in cart. Current Qty: {}, Requested to add: {}. Updating quantity.",
                    product.getId(), cartItemToSave.getQuantity(), requestedQuantity);
            int newQuantity = cartItemToSave.getQuantity() + requestedQuantity;
            if (product.getStockQuantity() == null || product.getStockQuantity() < newQuantity) { // Kiểm tra null cho stock
                log.warn("SERVICE - addItemToCart - Not enough stock for Product ID {}. Requested total: {}, Stock: {}",
                        product.getId(), newQuantity, product.getStockQuantity());
                throw new RuntimeException("Số lượng sản phẩm " + product.getName() + " không đủ trong kho.");
            }
            cartItemToSave.setQuantity(newQuantity);
            cartItemToSave.setPrice(product.getPrice() * newQuantity); // Cập nhật tổng tiền dòng
        } else {
            log.info("SERVICE - addItemToCart - Product ID {} not in cart. Creating new cart item.", product.getId());
            if (product.getStockQuantity() == null || product.getStockQuantity() < requestedQuantity) { // Kiểm tra null cho stock
                log.warn("SERVICE - addItemToCart - Not enough stock for new Product ID {}. Requested: {}, Stock: {}",
                        product.getId(), requestedQuantity, product.getStockQuantity());
                throw new RuntimeException("Số lượng sản phẩm " + product.getName() + " không đủ trong kho.");
            }
            cartItemToSave = CartItem.builder()
                    .user(userEntity) // Sẽ là null nếu là guest
                    .product(product)
                    .quantity(requestedQuantity)
                    .price(product.getPrice() * requestedQuantity) // Tổng tiền dòng
                    .guestCartId(isGuest ? guestCartIdToUse : null)
                    .build();
        }
        cartItemRepository.save(cartItemToSave);
        log.info("SERVICE - addItemToCart - Cart item saved/updated. ID: {}", cartItemToSave.getId());

        return getCart(userId, guestCartIdToUse);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(Integer userId, String guestCartId) {
        List<CartItem> cartItems;
        String currentGuestCartIdForResponse = null;

        if (userId != null) {
            log.info("SERVICE - getCart - Fetching cart for User ID: {}", userId);
            cartItems = cartItemRepository.findByUserId(userId); // TRUY VẤN THỰC TẾ
        } else if (guestCartId != null && !guestCartId.trim().isEmpty()) {
            log.info("SERVICE - getCart - Fetching cart for GuestCartID: {}", guestCartId);
            cartItems = cartItemRepository.findByGuestCartId(guestCartId); // TRUY VẤN THỰC TẾ
            currentGuestCartIdForResponse = guestCartId;
        } else {
            log.info("SERVICE - getCart - No UserID or GuestCartID provided. Returning empty cart.");
            return CartResponse.builder().items(Collections.emptyList()).totalUniqueItems(0).totalQuantity(0).subtotalPrice(0).build();
        }
        log.debug("SERVICE - getCart - Found {} raw cart items.", cartItems.size());

        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(cartItem -> {
                    try {
                        // Đảm bảo CartItemMapper có thể xử lý các mối quan hệ LAZY (product) nếu nó không EAGER
                        // Cần thêm EntityGraph trong CartItemRepository nếu product là LAZY
                        return cartItemMapper.toDTO(cartItem);
                    } catch (Exception e) {
                        log.error("SERVICE - getCart - Error mapping CartItem ID {} to DTO: {}", cartItem.getId(), e.getMessage(), e);
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        log.debug("SERVICE - getCart - Mapped {} CartItemResponses.", itemResponses.size());

        int totalQuantity = itemResponses.stream().mapToInt(CartItemResponse::getQuantity).sum();
        int subtotalPrice = itemResponses.stream().mapToInt(item -> item.getLineTotal() != null ? item.getLineTotal() : 0).sum();

        CartResponse cartResponse = CartResponse.builder()
                .items(itemResponses)
                .totalUniqueItems(itemResponses.size())
                .totalQuantity(totalQuantity)
                .subtotalPrice(subtotalPrice)
                .guestCartId(currentGuestCartIdForResponse)
                .build();
        log.info("SERVICE - getCart - Built CartResponse. GuestCartID: {}, TotalItems: {}, TotalQty: {}, Subtotal: {}",
                cartResponse.getGuestCartId(), cartResponse.getTotalUniqueItems(), cartResponse.getTotalQuantity(), cartResponse.getSubtotalPrice());
        return cartResponse;
    }


    @Override
    @Transactional
    public CartResponse updateItemQuantity(Integer userId, String guestCartId, Long cartItemId, CartItemUpdateQuantityRequest quantityRequest) {
        log.info("SERVICE - updateItemQuantity - UserID: {}, GuestCartID: {}, CartItemID: {}, NewQty: {}",
                userId, guestCartId, cartItemId, quantityRequest.getQuantity());

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy mặt hàng trong giỏ với ID: " + cartItemId));

        // Xác thực quyền sở hữu
        if (userId != null) { // Người dùng đã đăng nhập
            // Kiểm tra userEntity null nếu user không tồn tại trong DB nhưng có userId
            if (cartItem.getUser() == null || !cartItem.getUser().getId().equals(userId)) {
                log.warn("SERVICE - updateItemQuantity - UserID: {} attempted to update CartItemID: {} not belonging to them.", userId, cartItemId);
                throw new SecurityException("Bạn không có quyền cập nhật mặt hàng này (user mismatch).");
            }
        } else if (guestCartId != null) { // Khách vãng lai
            if (cartItem.getGuestCartId() == null || !cartItem.getGuestCartId().equals(guestCartId)) {
                log.warn("SERVICE - updateItemQuantity - GuestCartID: {} attempted to update CartItemID: {} with different/null guestId.", guestCartId, cartItemId);
                throw new SecurityException("Bạn không có quyền cập nhật mặt hàng này (guest cart ID mismatch).");
            }
        } else { // Không có userId và không có guestCartId -> không hợp lệ
            log.warn("SERVICE - updateItemQuantity - Attempt to update CartItemID: {} without userId or guestCartId.", cartItemId);
            throw new SecurityException("Không thể xác định giỏ hàng để cập nhật.");
        }


        if (quantityRequest.getQuantity() <= 0) {
            cartItemRepository.delete(cartItem);
            log.info("SERVICE - CartItemID: {} removed due to quantity <= 0.", cartItemId);
        } else {
            Product product = cartItem.getProduct();
            if (product == null) { // Kiểm tra thêm product null
                log.error("SERVICE - updateItemQuantity - Product is null for CartItemID: {}. Cannot update.", cartItemId);
                throw new EntityNotFoundException("Sản phẩm liên kết với mặt hàng trong giỏ không tồn tại.");
            }
            if (product.getStockQuantity() == null || product.getStockQuantity() < quantityRequest.getQuantity()) { // Kiểm tra null cho stock
                throw new RuntimeException("Số lượng sản phẩm " + product.getName() + " không đủ trong kho.");
            }
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
        log.info("SERVICE - Remove CartItemID: {}, UserID: {}, GuestCartID: {}", cartItemId, userId, guestCartId);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy mặt hàng trong giỏ với ID: " + cartItemId));

        if (userId != null) {
            if (cartItem.getUser() == null || !cartItem.getUser().getId().equals(userId)) {
                throw new SecurityException("Bạn không có quyền xóa mặt hàng này (user mismatch).");
            }
        } else if (guestCartId != null) {
            if (cartItem.getGuestCartId() == null || !cartItem.getGuestCartId().equals(guestCartId)) {
                throw new SecurityException("Bạn không có quyền xóa mặt hàng này (guest cart ID mismatch).");
            }
        } else {
            throw new SecurityException("Không thể xác định giỏ hàng để xóa.");
        }

        cartItemRepository.delete(cartItem);
        log.info("SERVICE - CartItemID: {} removed.", cartItemId);
        return getCart(userId, guestCartId);
    }

    @Override
    @Transactional
    public CartResponse clearCart(Integer userId, String guestCartId) {
        if (userId != null) {
            log.info("SERVICE - Clearing cart for UserID: {}", userId);
            cartItemRepository.deleteByUserId(userId); // TRUY VẤN THỰC TẾ
        } else if (guestCartId != null && !guestCartId.trim().isEmpty()) {
            log.info("SERVICE - Clearing cart for GuestCartID: {}", guestCartId);
            cartItemRepository.deleteByGuestCartId(guestCartId); // TRUY VẤN THỰC TẾ
        } else {
            log.warn("SERVICE - clearCart called without userId or guestCartId.");
        }
        // Luôn trả về trạng thái giỏ hàng sau khi xóa (sẽ là giỏ hàng trống hoặc giỏ hàng của user nếu guestCartId không được cung cấp)
        return getCart(userId, guestCartId);
    }

    @Override
    @Transactional
    public CartResponse mergeCart(Integer userId, String guestCartId) {
        log.info("SERVICE - Merging GuestCartID: {} into UserID: {}", guestCartId, userId);
        if (userId == null) {
            log.error("SERVICE - mergeCart - UserID cannot be null for merging.");
            throw new IllegalArgumentException("UserID là bắt buộc để hợp nhất giỏ hàng.");
        }
        if (guestCartId == null || guestCartId.trim().isEmpty()) {
            log.info("SERVICE - mergeCart - GuestCartID is null or empty. Nothing to merge for UserID: {}", userId);
            return getCart(userId, null); // Trả về giỏ hàng hiện tại của người dùng
        }

        User user = userRepository.findById(userId) // User entity có thể là LAZY
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        List<CartItem> guestCartItems = cartItemRepository.findByGuestCartId(guestCartId);
        if (guestCartItems.isEmpty()) {
            log.info("SERVICE - Guest cart {} is empty, nothing to merge for UserID: {}.", guestCartId, userId);
            return getCart(userId, null);
        }

        log.info("SERVICE - Found {} items in guest cart {} to merge for UserID: {}", guestCartItems.size(), guestCartId, userId);

        for (CartItem guestItem : guestCartItems) {
            if (guestItem.getProduct() == null) {
                log.warn("SERVICE - Merge: GuestItem ID {} has null product. Skipping.", guestItem.getId());
                cartItemRepository.delete(guestItem); // Xóa item lỗi của guest
                continue;
            }

            Optional<CartItem> userCartItemOpt = cartItemRepository.findByUserIdAndProductId(userId, guestItem.getProduct().getId());
            CartItem itemToSaveForUser;

            if (userCartItemOpt.isPresent()) {
                // Sản phẩm đã có trong giỏ của user, cộng dồn số lượng
                itemToSaveForUser = userCartItemOpt.get();
                log.info("SERVICE - Merge: Product ID {} (GuestItem ID {}) already in user cart (UserCartItem ID {}). Updating quantity.",
                        guestItem.getProduct().getId(), guestItem.getId(), itemToSaveForUser.getId());

                int newQuantity = itemToSaveForUser.getQuantity() + guestItem.getQuantity();
                if (itemToSaveForUser.getProduct().getStockQuantity() == null || itemToSaveForUser.getProduct().getStockQuantity() < newQuantity) { // Kiểm tra null cho stock
                    log.warn("SERVICE - Merge: Not enough stock for Product ID {}. Requested total: {}, Stock: {}. Setting to max available for user item.",
                            guestItem.getProduct().getId(), newQuantity, itemToSaveForUser.getProduct().getStockQuantity());
                    newQuantity = itemToSaveForUser.getProduct().getStockQuantity() != null ? itemToSaveForUser.getProduct().getStockQuantity() : 0; // Giới hạn bằng stock nếu vượt quá
                }
                itemToSaveForUser.setQuantity(newQuantity);
                itemToSaveForUser.setPrice(itemToSaveForUser.getProduct().getPrice() * newQuantity);
                cartItemRepository.save(itemToSaveForUser);
                cartItemRepository.delete(guestItem); // Xóa item của guest sau khi đã cộng dồn vào item của user
                log.info("SERVICE - Merge: Deleted guest item ID {} after merging quantity to user item ID {}.", guestItem.getId(), itemToSaveForUser.getId());
            } else {
                // Sản phẩm chưa có trong giỏ của user, chuyển guestItem thành item của user
                log.info("SERVICE - Merge: Product ID {} (GuestItem ID {}) not in user cart. Assigning guest item to user.",
                        guestItem.getProduct().getId(), guestItem.getId());
                guestItem.setUser(user);
                guestItem.setGuestCartId(null); // Quan trọng: Xóa guestCartId
                // Đảm bảo số lượng không vượt quá tồn kho
                if (guestItem.getProduct().getStockQuantity() == null || guestItem.getProduct().getStockQuantity() < guestItem.getQuantity()) { // Kiểm tra null cho stock
                    log.warn("SERVICE - Merge: Not enough stock for new Product ID {}. Requested: {}, Stock: {}. Setting to max available.",
                            guestItem.getProduct().getId(), guestItem.getQuantity(), guestItem.getProduct().getStockQuantity());
                    guestItem.setQuantity(guestItem.getProduct().getStockQuantity() != null ? guestItem.getProduct().getStockQuantity() : 0); // Giới hạn bằng stock
                    // Cập nhật lại price nếu quantity thay đổi
                    guestItem.setPrice(guestItem.getProduct().getPrice() * guestItem.getQuantity());
                }
                cartItemRepository.save(guestItem); // Lưu lại guestItem đã được cập nhật user và guestCartId=null
                log.info("SERVICE - Merge: GuestItem ID {} successfully reassigned to UserID {}.", guestItem.getId(), userId);
            }
        }
        log.info("SERVICE - Cart merged successfully for UserID: {}", userId);
        return getCart(userId, null);
    }
}
