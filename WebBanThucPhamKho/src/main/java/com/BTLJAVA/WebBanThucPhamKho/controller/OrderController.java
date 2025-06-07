package com.BTLJAVA.WebBanThucPhamKho.controller;

import com.BTLJAVA.WebBanThucPhamKho.dto.request.OrderRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.OrderResponse;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.ResponseData;
import com.BTLJAVA.WebBanThucPhamKho.entity.CustomUserDetails;
import com.BTLJAVA.WebBanThucPhamKho.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections; // For empty list if needed
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@EnableMethodSecurity // Kích hoạt @PreAuthorize
public class OrderController {

    private final OrderService orderService;

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

    @PostMapping
    public ResponseData<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest orderRequest) {
        Integer userId = getCurrentUserIdOptional();
        log.info("CONTROLLER - API POST /orders - Attempting to place order. UserID: {}, GuestCartID: {}, CustomerName: {}",
                userId, orderRequest.getGuestCartId(), orderRequest.getCustomerName());
        try {
            OrderResponse orderResponse = orderService.placeOrder(userId, orderRequest);
            // Service nên ném exception nếu có lỗi nghiêm trọng, không nên trả về null.
            if (orderResponse == null) {
                log.error("CONTROLLER - placeOrder - Service returned null unexpectedly. UserID: {}, GuestCartID: {}", userId, orderRequest.getGuestCartId());
                return ResponseData.<OrderResponse>builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message("Lỗi xử lý đơn hàng: Không nhận được phản hồi từ dịch vụ.")
                        .error("ServiceReturnedNull")
                        .build();
            }
            log.info("CONTROLLER - placeOrder - Order placed successfully. OrderID: {}", orderResponse.getId());
            return ResponseData.<OrderResponse>builder()
                    .status(HttpStatus.CREATED.value()) // 201 Created là phù hợp cho tạo mới thành công
                    .message("Đặt hàng thành công!")
                    .data(orderResponse)
                    .build();
        } catch (IllegalStateException | IllegalArgumentException e) { // Lỗi nghiệp vụ từ service
            log.warn("CONTROLLER - placeOrder - Business logic error: {}", e.getMessage());
            return ResponseData.<OrderResponse>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Lỗi khi đặt hàng: " + e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build();
        } catch (EntityNotFoundException enfe) { // Không tìm thấy entity (ví dụ: user, product)
            log.warn("CONTROLLER - placeOrder - Entity not found: {}", enfe.getMessage());
            return ResponseData.<OrderResponse>builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message("Lỗi khi đặt hàng: " + enfe.getMessage())
                    .error(enfe.getClass().getSimpleName())
                    .build();
        } catch (Exception e) { // Các lỗi không mong muốn khác
            log.error("CONTROLLER - placeOrder - Unexpected Exception: {} - StackTrace: ", e.getMessage(), e);
            return ResponseData.<OrderResponse>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi hệ thống khi đặt hàng. Vui lòng thử lại sau.")
                    .error(e.getClass().getSimpleName())
                    .build();
        }
    }


    @GetMapping("/my-history")
    public ResponseData<List<OrderResponse>> getMyOrderHistory() { // Bỏ Authentication authentication nếu không dùng đến trực tiếp
        Integer userId = getCurrentUserIdOptional();
        if (userId == null) {
            // Điều này không nên xảy ra do @PreAuthorize, nhưng là một lớp bảo vệ
            log.warn("CONTROLLER - getMyOrderHistory - UserID is null despite @PreAuthorize(\"isAuthenticated()\")");
            return ResponseData.<List<OrderResponse>>builder()
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .message("Yêu cầu đăng nhập để xem lịch sử đơn hàng.")
                    .error("AuthenticationRequired")
                    .build();
        }
        log.info("CONTROLLER - API GET /orders/my-history - UserID: {}", userId);
        try {
            List<OrderResponse> orderHistory = orderService.getOrderHistoryForUser(userId);
            return ResponseData.<List<OrderResponse>>builder()
                    .status(HttpStatus.OK.value())
                    .message("Lấy lịch sử đơn hàng thành công.")
                    .data(orderHistory)
                    .build();
        } catch (EntityNotFoundException enfe){
            log.warn("CONTROLLER - getMyOrderHistory - User not found for history (UserID: {}): {}", userId, enfe.getMessage());
            return ResponseData.<List<OrderResponse>>builder().status(HttpStatus.NOT_FOUND.value()).message(enfe.getMessage()).error(enfe.getClass().getSimpleName()).build();
        }
        catch (Exception e) {
            log.error("CONTROLLER - getMyOrderHistory - Exception for UserID {}: {}", userId, e.getMessage(), e);
            return ResponseData.<List<OrderResponse>>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi server khi lấy lịch sử đơn hàng.")
                    .error(e.getClass().getSimpleName())
                    .build();
        }
    }


    @GetMapping("/{orderId}")
    public ResponseData<OrderResponse> getMyOrderById(@PathVariable Integer orderId) { // Bỏ Authentication authentication
        Integer userId = getCurrentUserIdOptional();
        if (userId == null) {
            log.warn("CONTROLLER - getMyOrderById - UserID is null despite @PreAuthorize for orderId {}", orderId);
            return ResponseData.<OrderResponse>builder()
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .message("Yêu cầu đăng nhập.")
                    .error("AuthenticationRequired")
                    .build();
        }
        log.info("CONTROLLER - API GET /orders/{} - UserID: {}", orderId, userId);
        try {
            OrderResponse orderResponse = orderService.getOrderByIdForUser(orderId, userId);
            return ResponseData.<OrderResponse>builder()
                    .status(HttpStatus.OK.value())
                    .message("Lấy chi tiết đơn hàng thành công.")
                    .data(orderResponse)
                    .build();
        } catch (EntityNotFoundException enfe) {
            log.warn("CONTROLLER - getMyOrderById - Entity not found for orderId {}: {}", orderId, enfe.getMessage());
            return ResponseData.<OrderResponse>builder().status(HttpStatus.NOT_FOUND.value()).message(enfe.getMessage()).error(enfe.getClass().getSimpleName()).build();
        } catch (AccessDeniedException ade) {
            log.warn("CONTROLLER - getMyOrderById - Access denied for orderId {}: {}", orderId, ade.getMessage());
            return ResponseData.<OrderResponse>builder().status(HttpStatus.FORBIDDEN.value()).message(ade.getMessage()).error(ade.getClass().getSimpleName()).build();
        } catch (Exception e) {
            log.error("CONTROLLER - getMyOrderById - Exception for OrderID {}, UserID {}: {}", orderId, userId, e.getMessage(), e);
            return ResponseData.<OrderResponse>builder().status(HttpStatus.INTERNAL_SERVER_ERROR.value()).message("Lỗi server: " + e.getMessage()).error(e.getClass().getSimpleName()).build();
        }
    }

    // --- Admin Endpoints ---
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ResponseData<Page<OrderResponse>> getAllOrdersForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createAt,desc") String[] sortParams) {
        log.info("CONTROLLER - API GET /orders/admin/all - Page: {}, Size: {}, Sort: {}", page, size, sortParams);
        try {
            String sortField = (sortParams.length > 0 && sortParams[0] != null && !sortParams[0].isEmpty()) ? sortParams[0] : "createAt";
            Sort.Direction direction = (sortParams.length > 1 && sortParams[1] != null && sortParams[1].equalsIgnoreCase("asc")) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

            Page<OrderResponse> ordersPage = orderService.getAllOrdersForAdmin(pageable);
            return ResponseData.<Page<OrderResponse>>builder()
                    .status(HttpStatus.OK.value())
                    .message("Lấy tất cả đơn hàng (admin) thành công.")
                    .data(ordersPage)
                    .build();
        } catch (Exception e) {
            log.error("CONTROLLER - getAllOrdersForAdmin - Exception: {}", e.getMessage(), e);
            return ResponseData.<Page<OrderResponse>>builder().status(HttpStatus.INTERNAL_SERVER_ERROR.value()).message("Lỗi server khi lấy tất cả đơn hàng.").error(e.getClass().getSimpleName()).build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{orderId}")
    public ResponseData<OrderResponse> getOrderByIdForAdmin(@PathVariable Integer orderId) {
        log.info("CONTROLLER - API GET /orders/admin/{} - For Admin", orderId);
        try {
            OrderResponse order = orderService.getOrderByIdForAdmin(orderId);
            return ResponseData.<OrderResponse>builder()
                    .status(HttpStatus.OK.value())
                    .message("Lấy chi tiết đơn hàng (admin) thành công.")
                    .data(order)
                    .build();
        } catch (EntityNotFoundException enfe) {
            log.warn("CONTROLLER - getOrderByIdForAdmin - Entity not found for orderId {}: {}", orderId, enfe.getMessage());
            return ResponseData.<OrderResponse>builder().status(HttpStatus.NOT_FOUND.value()).message(enfe.getMessage()).error(enfe.getClass().getSimpleName()).build();
        } catch (Exception e) {
            log.error("CONTROLLER - getOrderByIdForAdmin - Exception for OrderID {}: {}", orderId, e.getMessage(), e);
            return ResponseData.<OrderResponse>builder().status(HttpStatus.INTERNAL_SERVER_ERROR.value()).message("Lỗi server: " + e.getMessage()).error(e.getClass().getSimpleName()).build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/{orderId}/status")
    public ResponseData<OrderResponse> updateOrderStatus(
            @PathVariable Integer orderId,
            @RequestBody Map<String, String> statusUpdate) {
        String newStatus = statusUpdate.get("newStatus");
        log.info("CONTROLLER - API PATCH /orders/admin/{}/status - New status: {}", orderId, newStatus);
        if (newStatus == null || newStatus.trim().isEmpty()) {
            return ResponseData.<OrderResponse>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Trạng thái mới không được để trống.")
                    .error("InvalidInput")
                    .build();
        }
        try {
            OrderResponse updatedOrder = orderService.updateOrderStatusForAdmin(orderId, newStatus);
            return ResponseData.<OrderResponse>builder()
                    .status(HttpStatus.OK.value())
                    .message("Cập nhật trạng thái đơn hàng thành công.")
                    .data(updatedOrder)
                    .build();
        } catch (EntityNotFoundException enfe) {
            log.warn("CONTROLLER - updateOrderStatus - Entity not found for orderId {}: {}", orderId, enfe.getMessage());
            return ResponseData.<OrderResponse>builder().status(HttpStatus.NOT_FOUND.value()).message(enfe.getMessage()).error(enfe.getClass().getSimpleName()).build();
        } catch (IllegalArgumentException iae) { // Bắt lỗi trạng thái không hợp lệ từ service
            log.warn("CONTROLLER - updateOrderStatus - Invalid argument for orderId {}: {}", orderId, iae.getMessage());
            return ResponseData.<OrderResponse>builder().status(HttpStatus.BAD_REQUEST.value()).message(iae.getMessage()).error(iae.getClass().getSimpleName()).build();
        } catch (Exception e) {
            log.error("CONTROLLER - updateOrderStatus - Exception for OrderID {}: {}", orderId, e.getMessage(), e);
            return ResponseData.<OrderResponse>builder().status(HttpStatus.INTERNAL_SERVER_ERROR.value()).message("Lỗi server: " + e.getMessage()).error(e.getClass().getSimpleName()).build();
        }
    }
}