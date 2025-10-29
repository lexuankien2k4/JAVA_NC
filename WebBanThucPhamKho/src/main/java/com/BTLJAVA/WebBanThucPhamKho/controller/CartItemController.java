package com.BTLJAVA.WebBanThucPhamKho.controller;

import com.BTLJAVA.WebBanThucPhamKho.dto.request.CartItemRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.request.CartItemUpdateQuantityRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.CartResponse;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.ResponseData;
import com.BTLJAVA.WebBanThucPhamKho.entity.CustomUserDetails;
import com.BTLJAVA.WebBanThucPhamKho.service.CartItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
            return ((CustomUserDetails) authentication.getPrincipal()).getId();
        }
        return null;
    }


    @PostMapping("/add")
    public ResponseData<CartResponse> addItemToCart(@Valid @RequestBody CartItemRequest cartItemRequest) {
        Integer userId = getCurrentUserIdOptional();
        log.info("CONTROLLER - API POST /cart/add - UserID: {}, GuestCartID: {}", userId, cartItemRequest.getGuestCartId());

        CartResponse cartResponse = cartService.addItemToCart(userId, cartItemRequest);

        return ResponseData.<CartResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Sản phẩm đã được thêm/cập nhật trong giỏ hàng.")
                .data(cartResponse)
                .build();
    }

    @GetMapping
    public ResponseData<CartResponse> getCart(@RequestParam(required = false) String guestCartId) {
        Integer userId = getCurrentUserIdOptional();
        log.info("CONTROLLER - API GET /cart - UserID: {}, Request GuestCartID: {}", userId, guestCartId);

        CartResponse cartResponse = cartService.getCart(userId, guestCartId);

        return ResponseData.<CartResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy thông tin giỏ hàng thành công.")
                .data(cartResponse)
                .build();
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

        CartResponse cartResponse = cartService.updateItemQuantity(userId, guestCartId, cartItemId, quantityRequest);

        return ResponseData.<CartResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật số lượng sản phẩm thành công.")
                .data(cartResponse)
                .build();
    }

    @PreAuthorize("isAuthenticated() or #guestCartId != null")
    @DeleteMapping("/item/{cartItemId}")
    public ResponseData<CartResponse> removeItemFromCart(
            @PathVariable Long cartItemId,
            @RequestParam(required = false) String guestCartId) {
        Integer userId = getCurrentUserIdOptional();
        log.info("CONTROLLER - API DELETE /cart/item/{} - UserID: {}, GuestCartID: {}", cartItemId, userId, guestCartId);

        CartResponse cartResponse = cartService.removeItemFromCart(userId, guestCartId, cartItemId);

        return ResponseData.<CartResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Xóa sản phẩm khỏi giỏ hàng thành công.")
                .data(cartResponse)
                .build();
    }

    @PreAuthorize("isAuthenticated() or #guestCartId != null")
    @DeleteMapping("/clear")
    public ResponseData<CartResponse> clearCart(@RequestParam(required = false) String guestCartId) {
        Integer userId = getCurrentUserIdOptional();
        log.info("CONTROLLER - API DELETE /cart/clear - UserID: {}, GuestCartID: {}", userId, guestCartId);

        CartResponse cartResponse = cartService.clearCart(userId, guestCartId);

        return ResponseData.<CartResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Giỏ hàng đã được xóa sạch.")
                .data(cartResponse)
                .build();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/merge")
    public ResponseData<CartResponse> mergeGuestCart(@RequestParam String guestCartId) {
        Integer userId = getCurrentUserIdOptional(); // @PreAuthorize đã đảm bảo userId không null
        log.info("CONTROLLER - API POST /cart/merge - UserID: {} merging with GuestCartID: {}", userId, guestCartId);

        CartResponse cartResponse = cartService.mergeCart(userId, guestCartId);

        return ResponseData.<CartResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Giỏ hàng đã được hợp nhất thành công.")
                .data(cartResponse)
                .build();
    }
}