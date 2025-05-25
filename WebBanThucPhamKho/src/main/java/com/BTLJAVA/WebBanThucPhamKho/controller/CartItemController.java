package com.BTLJAVA.WebBanThucPhamKho.controller;

import com.BTLJAVA.WebBanThucPhamKho.dto.request.CartItemRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.request.CartItemUpdateQuantityRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.CartResponse;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.ResponseData;
import com.BTLJAVA.WebBanThucPhamKho.entity.CustomUserDetails;
import com.BTLJAVA.WebBanThucPhamKho.service.CartItemService; // Đảm bảo đây là CartService đã được cập nhật
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // Bỏ comment nếu muốn dùng @PreAuthorize ở đây
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Slf4j
@EnableMethodSecurity // Kích hoạt @PreAuthorize cho các phương thức trong controller này
public class CartItemController {

    private final CartItemService cartService;

    private Integer getCurrentUserIdOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal()) &&
                authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getId();
        }
        return null;
    }

    @PostMapping("/add")
    public ResponseData<CartResponse> addItemToCart(@Valid @RequestBody CartItemRequest cartItemRequest) {
        Integer userId = getCurrentUserIdOptional();
        log.info("CONTROLLER - API POST /cart/add - UserID: {}, GuestCartID in request: {}, ProductID: {}, Quantity: {}",
                userId, cartItemRequest.getGuestCartId(), cartItemRequest.getProductId(), cartItemRequest.getQuantity());
        try {
            CartResponse cartResponse = cartService.addItemToCart(userId, cartItemRequest);
            if (cartResponse == null) {
                log.error("CONTROLLER - addItemToCart - Service returned null. UserID: {}, GuestCartID: {}", userId, cartItemRequest.getGuestCartId());
                return ResponseData.<CartResponse>builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message("Lỗi xử lý giỏ hàng: Service không trả về dữ liệu.")
                        .build();
            }
            log.info("CONTROLLER - addItemToCart - Success. Cart items: {}, Total quantity: {}", cartResponse.getItems().size(), cartResponse.getTotalQuantity());
            return ResponseData.<CartResponse>builder()
                    .status(HttpStatus.OK.value())
                    .message("Sản phẩm đã được thêm/cập nhật trong giỏ hàng.")
                    .data(cartResponse)
                    .build();
        } catch (Exception e) {
            log.error("CONTROLLER - addItemToCart - Exception: UserID: {}, GuestCartID: {}, Error: {}",
                    userId, cartItemRequest.getGuestCartId(), e.getMessage(), e);
            return ResponseData.<CartResponse>builder()
                    .status(HttpStatus.BAD_REQUEST.value()) // Hoặc 500 tùy theo loại lỗi
                    .message("Lỗi khi thêm vào giỏ hàng: " + e.getMessage())
                    .build();
        }
    }

    @GetMapping
    // Không cần @PreAuthorize nếu guest có thể xem giỏ hàng của họ qua guestCartId
    // Nếu chỉ user đã đăng nhập mới được xem giỏ của mình thì dùng: @PreAuthorize("isAuthenticated()")
    public ResponseData<CartResponse> getCart(@RequestParam(required = false) String guestCartId) {
        Integer userId = getCurrentUserIdOptional();
        log.info("CONTROLLER - API GET /cart - UserID: {}, Request GuestCartID: {}", userId, guestCartId);
        try {
            CartResponse cartResponse = cartService.getCart(userId, guestCartId);
            if (cartResponse == null) {
                log.error("CONTROLLER - getCart - Service returned null. UserID: {}, GuestCartID: {}", userId, guestCartId);
                return ResponseData.<CartResponse>builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message("Lỗi xử lý giỏ hàng: Service không trả về dữ liệu.")
                        .build();
            }
            return ResponseData.<CartResponse>builder()
                    .status(HttpStatus.OK.value())
                    .message("Lấy thông tin giỏ hàng thành công.")
                    .data(cartResponse)
                    .build();
        } catch (Exception e) {
            log.error("CONTROLLER - API GET /cart - Exception: UserID: {}, GuestCartID: {}, Error: {}",
                    userId, guestCartId, e.getMessage(), e);
            return ResponseData.<CartResponse>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value()) // Lỗi không xác định thường là 500
                    .message("Lỗi server khi lấy giỏ hàng: " + e.getMessage())
                    .build();
        }
    }

    @PreAuthorize("isAuthenticated() or #guestCartId != null")
    @PatchMapping("/item/{cartItemId}")
    public ResponseData<CartResponse> updateItemQuantity(
            @PathVariable Long cartItemId,
            @RequestParam(required = false) String guestCartId,
            @Valid @RequestBody CartItemUpdateQuantityRequest quantityRequest) {
        Integer userId = getCurrentUserIdOptional();
        log.info("CONTROLLER - API PATCH /cart/item/{} - UserID: {}, GuestCartID: {}, New Quantity: {}",
                cartItemId, userId, guestCartId, quantityRequest.getQuantity());
        try {
            CartResponse cartResponse = cartService.updateItemQuantity(userId, guestCartId, cartItemId, quantityRequest);
            if (cartResponse == null) { /* Xử lý null tương tự */ }
            return ResponseData.<CartResponse>builder()
                    .status(HttpStatus.OK.value())
                    .message("Cập nhật số lượng sản phẩm thành công.")
                    .data(cartResponse)
                    .build();
        } catch (Exception e) {
            log.error("CONTROLLER - API PATCH /cart/item/{} - Exception: {}", cartItemId, e.getMessage(), e);
            return ResponseData.<CartResponse>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Lỗi khi cập nhật số lượng: " + e.getMessage())
                    .build();
        }
    }

    @PreAuthorize("isAuthenticated() or #guestCartId != null")
    @DeleteMapping("/item/{cartItemId}")
    public ResponseData<CartResponse> removeItemFromCart(
            @PathVariable Long cartItemId,
            @RequestParam(required = false) String guestCartId) {
        Integer userId = getCurrentUserIdOptional();
        log.info("CONTROLLER - API DELETE /cart/item/{} - UserID: {}, GuestCartID: {}", cartItemId, userId, guestCartId);
        try {
            CartResponse cartResponse = cartService.removeItemFromCart(userId, guestCartId, cartItemId);
            if (cartResponse == null) { /* Xử lý null tương tự */ }
            return ResponseData.<CartResponse>builder()
                    .status(HttpStatus.OK.value())
                    .message("Xóa sản phẩm khỏi giỏ hàng thành công.")
                    .data(cartResponse)
                    .build();
        } catch (Exception e) {
            log.error("CONTROLLER - API DELETE /cart/item/{} - Exception: {}", cartItemId, e.getMessage(), e);
            return ResponseData.<CartResponse>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Lỗi khi xóa sản phẩm: " + e.getMessage())
                    .build();
        }
    }

    @PreAuthorize("isAuthenticated() or #guestCartId != null")
    @DeleteMapping("/clear")
    public ResponseData<CartResponse> clearCart(@RequestParam(required = false) String guestCartId) {
        Integer userId = getCurrentUserIdOptional();
        log.info("CONTROLLER - API DELETE /cart/clear - UserID: {}, GuestCartID: {}", userId, guestCartId);
        try {
            CartResponse cartResponse = cartService.clearCart(userId, guestCartId);
            if (cartResponse == null) { /* Xử lý null tương tự */ }
            return ResponseData.<CartResponse>builder()
                    .status(HttpStatus.OK.value())
                    .message("Giỏ hàng đã được xóa sạch.")
                    .data(cartResponse)
                    .build();
        } catch (Exception e) {
            log.error("CONTROLLER - API DELETE /cart/clear - Exception: {}", e.getMessage(), e);
            return ResponseData.<CartResponse>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi server khi xóa giỏ hàng: " + e.getMessage())
                    .build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/merge")
    public ResponseData<CartResponse> mergeGuestCart(Authentication authentication, @RequestParam String guestCartId) {
        Integer userId = getCurrentUserIdOptional();
        if (userId == null) {
            log.warn("CONTROLLER - API POST /cart/merge - Attempt to merge without authentication for guestCartId: {}", guestCartId);
            return ResponseData.<CartResponse>builder()
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .message("Yêu cầu xác thực để hợp nhất giỏ hàng.")
                    .build();
        }
        log.info("CONTROLLER - API POST /cart/merge - UserID: {} merging with GuestCartID: {}", userId, guestCartId);
        try {
            CartResponse cartResponse = cartService.mergeCart(userId, guestCartId);
            if (cartResponse == null) { /* Xử lý null tương tự */ }
            return ResponseData.<CartResponse>builder()
                    .status(HttpStatus.OK.value())
                    .message("Giỏ hàng đã được hợp nhất thành công.")
                    .data(cartResponse)
                    .build();
        } catch (Exception e) {
            log.error("CONTROLLER - API POST /cart/merge - Exception: UserID: {}, GuestCartID: {}, Error: {}",
                    userId, guestCartId, e.getMessage(), e);
            return ResponseData.<CartResponse>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Lỗi khi hợp nhất giỏ hàng: " + e.getMessage())
                    .build();
        }
    }
}