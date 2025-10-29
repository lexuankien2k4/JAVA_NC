// File: OrderController.java
package com.BTLJAVA.WebBanThucPhamKho.controller;

import com.BTLJAVA.WebBanThucPhamKho.dto.request.OrderRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.OrderResponse;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.ResponseData;
import com.BTLJAVA.WebBanThucPhamKho.entity.CustomUserDetails;
import com.BTLJAVA.WebBanThucPhamKho.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@EnableMethodSecurity
public class OrderController {

    private final OrderService orderService;

    private Integer getCurrentUserIdOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal()) &&
                authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getId();
        }
        return null;
    }

    @PostMapping
    public ResponseData<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest orderRequest) {
        Integer userId = getCurrentUserIdOptional();
        OrderResponse orderResponse = orderService.placeOrder(userId, orderRequest);

        return ResponseData.<OrderResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Đặt hàng thành công!")
                .data(orderResponse)
                .build();
    }


    @PreAuthorize("isAuthenticated()") // Bắt buộc đăng nhập
    @GetMapping("/my-history")
    public ResponseData<List<OrderResponse>> getMyOrderHistory() {
        Integer userId = getCurrentUserIdOptional(); // Sẽ không bao giờ null ở đây
        List<OrderResponse> orderHistory = orderService.getOrderHistoryForUser(userId);

        return ResponseData.<List<OrderResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy lịch sử đơn hàng thành công.")
                .data(orderHistory)
                .build();
    }


    @PreAuthorize("isAuthenticated()") // Bắt buộc đăng nhập
    @GetMapping("/{orderId}")
    public ResponseData<OrderResponse> getMyOrderById(@PathVariable Integer orderId) {
        Integer userId = getCurrentUserIdOptional(); // Sẽ không bao giờ null ở đây
        OrderResponse orderResponse = orderService.getOrderByIdForUser(orderId, userId);

        return ResponseData.<OrderResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy chi tiết đơn hàng thành công.")
                .data(orderResponse)
                .build();
    }

    // --- Admin Endpoints ---
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ResponseData<Page<OrderResponse>> getAllOrdersForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createAt,desc") String[] sortParams) {

        String sortField = (sortParams.length > 0 && sortParams[0] != null && !sortParams[0].isEmpty()) ? sortParams[0] : "createAt";
        Sort.Direction direction = (sortParams.length > 1 && sortParams[1] != null && sortParams[1].equalsIgnoreCase("asc")) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<OrderResponse> ordersPage = orderService.getAllOrdersForAdmin(pageable);

        return ResponseData.<Page<OrderResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy tất cả đơn hàng (admin) thành công.")
                .data(ordersPage)
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{orderId}")
    public ResponseData<OrderResponse> getOrderByIdForAdmin(@PathVariable Integer orderId) {
        OrderResponse order = orderService.getOrderByIdForAdmin(orderId);

        return ResponseData.<OrderResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy chi tiết đơn hàng (admin) thành công.")
                .data(order)
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/{orderId}/status")
    public ResponseData<OrderResponse> updateOrderStatus(
            @PathVariable Integer orderId,
            @RequestBody Map<String, String> statusUpdate) {

        String newStatus = statusUpdate.get("newStatus");
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Trạng thái mới không được để trống.");
        }

        OrderResponse updatedOrder = orderService.updateOrderStatusForAdmin(orderId, newStatus);

        return ResponseData.<OrderResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật trạng thái đơn hàng thành công.")
                .data(updatedOrder)
                .build();
    }
}