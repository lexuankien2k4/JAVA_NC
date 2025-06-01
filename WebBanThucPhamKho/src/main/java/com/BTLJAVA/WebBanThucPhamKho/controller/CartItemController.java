package com.BTLJAVA.WebBanThucPhamKho.controller;

import com.BTLJAVA.WebBanThucPhamKho.dto.request.CartItemRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.request.CartItemUpdateQuantityRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.CartResponse;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.ResponseData;
import com.BTLJAVA.WebBanThucPhamKho.entity.CustomUserDetails;
import com.BTLJAVA.WebBanThucPhamKho.service.CartItemService; // Đảm bảo đây là CartService đã được cập nhật
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections; // For empty list if needed
import java.util.List; // For List type

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Slf4j
@EnableMethodSecurity
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
        log.info("CONTROLLER - API POST /cart/add - UserID: {}, GuestCartID in req: {}, ProductID: {}, Qty: {}",
                userId, cartItemRequest.getGuestCartId(), cartItemRequest.getProductId(), cartItemRequest.getQuantity());
        try {
            CartResponse cartResponse = cartService.addItemToCart(userId, cartItemRequest);
            if (cartResponse == null) { // Service nên ném exception
                log.error("CONTROLLER - addItemToCart - Service returned null. UserID: {}, GuestCartID: {}", userId, cartItemRequest.getGuestCartId());
                return ResponseData.<CartResponse>builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message("Lỗi xử lý giỏ hàng: Service không trả về dữ liệu.")
                        .error("ServiceReturnedNull")
                        .build();
            }
            return ResponseData.<CartResponse>builder()
                    .status(HttpStatus.OK.value())
                    .message("Sản phẩm đã được thêm/cập nhật trong giỏ hàng.")
                    .data(cartResponse)
                    .build();
        } catch (EntityNotFoundException enfe) {
            log.warn("CONTROLLER - addItemToCart - Entity not found: {}", enfe.getMessage());
            return ResponseData.<CartResponse>builder().status(HttpStatus.NOT_FOUND.value()).message(enfe.getMessage()).error(enfe.getClass().getSimpleName()).build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("CONTROLLER - addItemToCart - Business logic error: {}", e.getMessage());
            return ResponseData.<CartResponse>builder().status(HttpStatus.BAD_REQUEST.value()).message(e.getMessage()).error(e.getClass().getSimpleName()).build();
        } catch (Exception e) {
            log.error("CONTROLLER - addItemToCart - Unexpected Exception: UserID: {}, GuestCartID: {}, Error: {}",
                    userId, cartItemRequest.getGuestCartId(), e.getMessage(), e);
            return ResponseData.<CartResponse>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi không mong muốn khi thêm vào giỏ hàng: " + e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build();
        }
    }

    @GetMapping
    public ResponseData<CartResponse> getCart(@RequestParam(required = false) String guestCartId) {
        Integer userId = getCurrentUserIdOptional();
        log.info("CONTROLLER - API GET /cart - UserID: {}, Request GuestCartID: {}", userId, guestCartId);
        try {
            CartResponse cartResponse = cartService.getCart(userId, guestCartId);
            if (cartResponse == null) { // Service nên trả về giỏ hàng trống
                log.warn("CONTROLLER - getCart - Service returned null, building empty cart. UserID: {}, GuestCartID: {}", userId, guestCartId);
                cartResponse = CartResponse.builder().items(Collections.emptyList()).totalQuantity(0).totalUniqueItems(0).subtotalPrice(0).guestCartId(guestCartId).build();
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
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi server khi lấy giỏ hàng.")
                    .error(e.getClass().getSimpleName())
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
        log.info("CONTROLLER - API PATCH /cart/item/{} - UserID: {}, GuestCartID: {}, NewQty: {}",
                cartItemId, userId, guestCartId, quantityRequest.getQuantity());
        try {
            CartResponse cartResponse = cartService.updateItemQuantity(userId, guestCartId, cartItemId, quantityRequest);
            return ResponseData.<CartResponse>builder()
                    .status(HttpStatus.OK.value())
                    .message("Cập nhật số lượng sản phẩm thành công.")
                    .data(cartResponse)
                    .build();
        } catch (EntityNotFoundException enfe) {
            log.warn("CONTROLLER - updateItemQuantity - Entity not found for cartItemId {}: {}",cartItemId, enfe.getMessage());
            return ResponseData.<CartResponse>builder().status(HttpStatus.NOT_FOUND.value()).message(enfe.getMessage()).error(enfe.getClass().getSimpleName()).build();
        } catch (SecurityException se) { // Bắt SecurityException từ service
            log.warn("CONTROLLER - updateItemQuantity - SecurityException for cartItemId {}: {}",cartItemId, se.getMessage());
            return ResponseData.<CartResponse>builder().status(HttpStatus.FORBIDDEN.value()).message(se.getMessage()).error(se.getClass().getSimpleName()).build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("CONTROLLER - updateItemQuantity - Business rule violation for cartItemId {}: {}",cartItemId, e.getMessage());
            return ResponseData.<CartResponse>builder().status(HttpStatus.BAD_REQUEST.value()).message(e.getMessage()).error(e.getClass().getSimpleName()).build();
        } catch (Exception e) {
            log.error("CONTROLLER - API PATCH /cart/item/{} - Unexpected Exception: {}", cartItemId, e.getMessage(), e);
            return ResponseData.<CartResponse>builder().status(HttpStatus.INTERNAL_SERVER_ERROR.value()).message("Lỗi server: " + e.getMessage()).error(e.getClass().getSimpleName()).build();
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
            return ResponseData.<CartResponse>builder()
                    .status(HttpStatus.OK.value())
                    .message("Xóa sản phẩm khỏi giỏ hàng thành công.")
                    .data(cartResponse)
                    .build();
        } catch (EntityNotFoundException enfe) {
            log.warn("CONTROLLER - removeItemFromCart - Entity not found for cartItemId {}: {}",cartItemId, enfe.getMessage());
            return ResponseData.<CartResponse>builder().status(HttpStatus.NOT_FOUND.value()).message(enfe.getMessage()).error(enfe.getClass().getSimpleName()).build();
        } catch (SecurityException se) {
            log.warn("CONTROLLER - removeItemFromCart - SecurityException for cartItemId {}: {}",cartItemId, se.getMessage());
            return ResponseData.<CartResponse>builder().status(HttpStatus.FORBIDDEN.value()).message(se.getMessage()).error(se.getClass().getSimpleName()).build();
        } catch (Exception e) {
            log.error("CONTROLLER - API DELETE /cart/item/{} - Unexpected Exception: {}", cartItemId, e.getMessage(), e);
            return ResponseData.<CartResponse>builder().status(HttpStatus.INTERNAL_SERVER_ERROR.value()).message("Lỗi server: " + e.getMessage()).error(e.getClass().getSimpleName()).build();
        }
    }

    @PreAuthorize("isAuthenticated() or #guestCartId != null")
    @DeleteMapping("/clear")
    public ResponseData<CartResponse> clearCart(@RequestParam(required = false) String guestCartId) {
        Integer userId = getCurrentUserIdOptional();
        log.info("CONTROLLER - API DELETE /cart/clear - UserID: {}, GuestCartID: {}", userId, guestCartId);
        try {
            CartResponse cartResponse = cartService.clearCart(userId, guestCartId);
            return ResponseData.<CartResponse>builder()
                    .status(HttpStatus.OK.value())
                    .message("Giỏ hàng đã được xóa sạch.")
                    .data(cartResponse) // Trả về giỏ hàng trống
                    .build();
        } catch (Exception e) {
            log.error("CONTROLLER - API DELETE /cart/clear - Unexpected Exception: {}", e.getMessage(), e);
            return ResponseData.<CartResponse>builder().status(HttpStatus.INTERNAL_SERVER_ERROR.value()).message("Lỗi server khi xóa giỏ hàng.").error(e.getClass().getSimpleName()).build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/merge")
    public ResponseData<CartResponse> mergeGuestCart(Authentication authentication, @RequestParam String guestCartId) {
        Integer userId = getCurrentUserIdOptional();
        if (userId == null) {
            log.warn("CONTROLLER - API POST /cart/merge - Unauthorized attempt to merge guestCartId: {}", guestCartId);
            return ResponseData.<CartResponse>builder()
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .message("Yêu cầu xác thực để hợp nhất giỏ hàng.")
                    .error("AuthenticationRequired")
                    .build();
        }
        log.info("CONTROLLER - API POST /cart/merge - UserID: {} merging with GuestCartID: {}", userId, guestCartId);
        try {
            CartResponse cartResponse = cartService.mergeCart(userId, guestCartId);
            return ResponseData.<CartResponse>builder()
                    .status(HttpStatus.OK.value())
                    .message("Giỏ hàng đã được hợp nhất thành công.")
                    .data(cartResponse)
                    .build();
        } catch (EntityNotFoundException enfe) {
            log.warn("CONTROLLER - mergeGuestCart - Entity not found: {}", enfe.getMessage());
            return ResponseData.<CartResponse>builder().status(HttpStatus.NOT_FOUND.value()).message(enfe.getMessage()).error(enfe.getClass().getSimpleName()).build();
        } catch (IllegalArgumentException iae) {
            log.warn("CONTROLLER - mergeGuestCart - Invalid argument: {}", iae.getMessage());
            return ResponseData.<CartResponse>builder().status(HttpStatus.BAD_REQUEST.value()).message(iae.getMessage()).error(iae.getClass().getSimpleName()).build();
        } catch (Exception e) {
            log.error("CONTROLLER - API POST /cart/merge - Unexpected Exception: UserID: {}, GuestCartID: {}, Error: {}",
                    userId, guestCartId, e.getMessage(), e);
            return ResponseData.<CartResponse>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi khi hợp nhất giỏ hàng: " + e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build();
        }
    }
}