package com.BTLJAVA.WebBanThucPhamKho.service.impl;

import com.BTLJAVA.WebBanThucPhamKho.dto.request.OrderRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.CartItemResponse;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.CartResponse;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.OrderResponse;
import com.BTLJAVA.WebBanThucPhamKho.entity.*;
import com.BTLJAVA.WebBanThucPhamKho.mapper.AddressMapper;
import com.BTLJAVA.WebBanThucPhamKho.mapper.OrderMapper;
import com.BTLJAVA.WebBanThucPhamKho.repository.*;
import com.BTLJAVA.WebBanThucPhamKho.service.CartItemService; // Đảm bảo tên này khớp
import com.BTLJAVA.WebBanThucPhamKho.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException; // Import cho SQLException
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final CartItemService cartItemService;
    private final OrderMapper orderMapper;
    private final AddressMapper addressMapper;

    public static final String ORDER_STATUS_PENDING = "PENDING";
    // ... (các hằng số status khác)

    @Override
    @Transactional // Rất quan trọng!
    public OrderResponse placeOrder(Integer userId, OrderRequest orderRequest) {
        log.info("SERVICE::placeOrder - START - UserID: {}, GuestCartID: {}", userId, orderRequest.getGuestCartId());

        User userEntity = null;
        if (userId != null) {
            userEntity = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.error("SERVICE::placeOrder - User not found with ID: {}", userId);
                        return new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId);
                    });
            log.info("SERVICE::placeOrder - User found: {}", userEntity.getUsername());
        } else {
            log.info("SERVICE::placeOrder - Placing order as guest. GuestCartID from request: {}", orderRequest.getGuestCartId());
        }

        CartResponse cart = cartItemService.getCart(userId, orderRequest.getGuestCartId());
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            log.warn("SERVICE::placeOrder - Cart is empty. UserID: {}, GuestCartID: {}", userId, orderRequest.getGuestCartId());
            throw new IllegalStateException("Giỏ hàng trống, không thể đặt hàng.");
        }
        log.info("SERVICE::placeOrder - Cart retrieved: {} unique items, total quantity {}. Subtotal: {}",
                cart.getTotalUniqueItems(), cart.getTotalQuantity(), cart.getSubtotalPrice());

        // 1. Validate OrderRequest
        if (orderRequest.getShippingAddress() == null) {
            log.error("SERVICE::placeOrder - ShippingAddress in OrderRequest is null.");
            throw new IllegalArgumentException("Thông tin địa chỉ giao hàng không được để trống.");
        }
        if (orderRequest.getCustomerName() == null || orderRequest.getCustomerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên khách hàng không được để trống.");
        }
        if (orderRequest.getCustomerPhone() == null || orderRequest.getCustomerPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại khách hàng không được để trống.");
        }


        // 2. Tạo và lưu địa chỉ giao hàng
        Address shippingAddressEntity = addressMapper.requestToEntity(orderRequest.getShippingAddress());
        if (shippingAddressEntity == null) {
            log.error("SERVICE::placeOrder - AddressMapper returned null for shippingAddressEntity.");
            throw new RuntimeException("Lỗi khi xử lý thông tin địa chỉ."); // Lỗi nội bộ
        }
        Address savedAddress;
        try {
            log.debug("SERVICE::placeOrder - Attempting to save address: Number={}, Street={}, Ward={}, District={}, City={}",
                    shippingAddressEntity.getNumber(), shippingAddressEntity.getStreet(), shippingAddressEntity.getWard(),
                    shippingAddressEntity.getDistrict(), shippingAddressEntity.getCity());
            savedAddress = addressRepository.saveAndFlush(shippingAddressEntity); // saveAndFlush để bắt lỗi DB sớm
            log.info("SERVICE::placeOrder - Shipping address saved successfully with ID: {}", savedAddress.getId());
        } catch (DataIntegrityViolationException e) {
            log.error("SERVICE::placeOrder - DataIntegrityViolationException while saving address: {}. SQLState: {}, ErrorCode: {}. Request: {}",
                    e.getMessage(), getSqlState(e), getSqlErrorCode(e), orderRequest.getShippingAddress(), e);
            throw new RuntimeException("Lỗi lưu địa chỉ: Dữ liệu không hợp lệ hoặc vi phạm ràng buộc. " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("SERVICE::placeOrder - Unexpected error while saving address: {}. Request: {}", e.getMessage(), orderRequest.getShippingAddress(), e);
            throw new RuntimeException("Lỗi không mong muốn khi lưu địa chỉ. " + e.getMessage(), e);
        }

        // 3. Tạo đối tượng Order
        Order order = new Order();
        order.setUser(userEntity);
        order.setAddress(savedAddress);
        order.setTitle(orderRequest.getTitle());
        order.setCustomerName(orderRequest.getCustomerName());
        order.setCustomerPhone(orderRequest.getCustomerPhone());
        order.setCustomerEmail(orderRequest.getCustomerEmail());
        order.setStatus(ORDER_STATUS_PENDING);
        // createAt và updateAt sẽ được AbstractEntity tự động quản lý

        log.info("SERVICE::placeOrder - Order entity populated. Customer: {}, Phone: {}, Email: {}, AddressID: {}",
                order.getCustomerName(), order.getCustomerPhone(), order.getCustomerEmail(), savedAddress.getId());

        // 4. Tạo OrderDetails từ CartItems và kiểm tra/cập nhật tồn kho
        Set<OrderDetail> orderDetailsSet = new HashSet<>();
        int calculatedTotalPrice = 0;

        for (CartItemResponse cartItemDto : cart.getItems()) {
            log.debug("SERVICE::placeOrder - Processing cart item: ProductID {}, ProductName '{}', Quantity {}, UnitPrice {}",
                    cartItemDto.getProductId(), cartItemDto.getProductName(), cartItemDto.getQuantity(), cartItemDto.getUnitPrice());

            if (cartItemDto.getProductId() == null) {
                throw new IllegalArgumentException("Một sản phẩm trong giỏ hàng không hợp lệ (thiếu ProductID).");
            }
            if (cartItemDto.getQuantity() == null || cartItemDto.getQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng sản phẩm '" + (cartItemDto.getProductName() != null ? cartItemDto.getProductName() : "ID " + cartItemDto.getProductId()) + "' không hợp lệ.");
            }
            if (cartItemDto.getUnitPrice() == null || cartItemDto.getUnitPrice() < 0) {
                throw new IllegalArgumentException("Đơn giá sản phẩm '" + (cartItemDto.getProductName() != null ? cartItemDto.getProductName() : "ID " + cartItemDto.getProductId()) + "' không hợp lệ.");
            }

            Product product = productRepository.findById(cartItemDto.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Sản phẩm '" + (cartItemDto.getProductName() != null ? cartItemDto.getProductName() : "ID " + cartItemDto.getProductId()) + "' không tồn tại."));

            if (product.getStockQuantity() < cartItemDto.getQuantity()) {
                throw new IllegalStateException("Sản phẩm '" + product.getName() + "' không đủ số lượng tồn kho ("+ product.getStockQuantity() +" còn lại, cần "+ cartItemDto.getQuantity() +").");
            }

            product.setStockQuantity(product.getStockQuantity() - cartItemDto.getQuantity());
            product.setSoldQuantity((product.getSoldQuantity() != null ? product.getSoldQuantity() : 0) + cartItemDto.getQuantity());
            productRepository.saveAndFlush(product); // saveAndFlush
            log.debug("SERVICE::placeOrder - Updated stock for product ID: {}. New stock: {}", product.getId(), product.getStockQuantity());

            OrderDetail detail = OrderDetail.builder()
                    .order(order) // Sẽ được gán sau khi order được save lần đầu nếu dùng cách này
                    .product(product)
                    .quantity(cartItemDto.getQuantity())
                    .price(cartItemDto.getUnitPrice())
                    .status(ORDER_STATUS_PENDING)
                    .build();
            orderDetailsSet.add(detail);
            calculatedTotalPrice += (cartItemDto.getUnitPrice() * cartItemDto.getQuantity());
        }

        order.setOrderDetails(orderDetailsSet);
        order.setTotalPrice(calculatedTotalPrice);
        // Gán lại order cho từng orderDetail sau khi order có thể đã có ID (nếu save order trước)
        // Tuy nhiên, với CascadeType.ALL, Hibernate sẽ quản lý việc này khi order được save.
        // Chỉ cần đảm bảo orderDetailsSet được gán vào order.
        // for (OrderDetail detail : orderDetailsSet) {
        //     detail.setOrder(order);
        // }
        log.info("SERVICE::placeOrder - Order entity prepared with {} details. Total price: {}", orderDetailsSet.size(), calculatedTotalPrice);

        // 5. Lưu Order (OrderDetails sẽ được lưu nhờ cascade)
        Order savedOrder;
        try {
            log.debug("SERVICE::placeOrder - Attempting to save Order. CustomerName: {}, TotalPrice: {}, Details count: {}",
                    order.getCustomerName(), order.getTotalPrice(), order.getOrderDetails().size());
            savedOrder = orderRepository.saveAndFlush(order); // saveAndFlush
            log.info("SERVICE::placeOrder - Order saved successfully with ID: {}", savedOrder.getId());
            if (savedOrder.getOrderDetails() != null) {
                savedOrder.getOrderDetails().forEach(od -> log.debug("SERVICE::placeOrder - Saved OrderDetail ID: {}, Product ID: {}", od.getId(), od.getProduct() != null ? od.getProduct().getId() : "null_product"));
            }
        } catch (DataIntegrityViolationException e) {
            log.error("SERVICE::placeOrder - DataIntegrityViolationException while saving order: {}. SQLState: {}, ErrorCode: {}", e.getMessage(), getSqlState(e), getSqlErrorCode(e), e);
            throw new RuntimeException("Lỗi lưu đơn hàng: Dữ liệu không hợp lệ hoặc vi phạm ràng buộc. " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("SERVICE::placeOrder - Unexpected error while saving order: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi không mong muốn khi lưu đơn hàng: " + e.getMessage(), e);
        }

        // 6. Xóa giỏ hàng
        try {
            cartItemService.clearCart(userId, orderRequest.getGuestCartId());
            log.info("SERVICE::placeOrder - Cart cleared for UserID: {} / GuestCartID: {}", userId, orderRequest.getGuestCartId());
        } catch (Exception e) {
            log.error("SERVICE::placeOrder - Error clearing cart after order placement. UserID: {}, GuestCartID: {}. Error: {}. Order (ID: {}) was still created.",
                    userId, orderRequest.getGuestCartId(), e.getMessage(), savedOrder.getId(), e);
            // Không ném lại lỗi này để tránh rollback đơn hàng đã thành công
        }

        // 7. Map sang DTO để trả về
        log.info("SERVICE::placeOrder - Preparing to map savedOrder (ID: {}) to OrderResponse", savedOrder.getId());
        return orderMapper.toDTO(savedOrder);
    }

    // Helper methods to extract SQLState and ErrorCode safely
    private String getSqlState(DataIntegrityViolationException e) {
        Throwable cause = e.getCause();
        while (cause != null && !(cause instanceof SQLException)) {
            cause = cause.getCause();
        }
        if (cause instanceof SQLException) {
            return ((SQLException) cause).getSQLState();
        }
        return "N/A";
    }

    private int getSqlErrorCode(DataIntegrityViolationException e) {
        Throwable cause = e.getCause();
        while (cause != null && !(cause instanceof SQLException)) {
            cause = cause.getCause();
        }
        if (cause instanceof SQLException) {
            return ((SQLException) cause).getErrorCode();
        }
        return 0;
    }


    // ... (Các phương thức getOrderByIdForUser, getOrderHistoryForUser, và các phương thức admin giữ nguyên
    //      nhưng cũng nên có logging chi tiết và ném Exception rõ ràng nếu có lỗi)
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByIdForUser(Integer orderId, Integer userId) {
        log.info("SERVICE - getOrderByIdForUser - Fetching order ID: {} for User ID: {}", orderId, userId);
        if (userId == null) {
            log.warn("SERVICE - getOrderByIdForUser - UserID không được null.");
            throw new IllegalArgumentException("UserID là bắt buộc để xem đơn hàng.");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        if (order.getUser() != null && !order.getUser().getId().equals(userId)) {
            log.warn("SERVICE - User {} cố gắng truy cập đơn hàng {} không thuộc về họ.", userId, orderId);
            throw new AccessDeniedException("Bạn không có quyền xem đơn hàng này.");
        }
        if (order.getUser() == null && userId != null) {
            log.warn("SERVICE - Người dùng đã xác thực {} cố gắng truy cập đơn hàng của khách {}.", userId, orderId);
            throw new AccessDeniedException("Không thể truy cập đơn hàng của khách.");
        }
        try {
            return orderMapper.toDTO(order);
        } catch (Exception e) {
            log.error("SERVICE - getOrderByIdForUser - Lỗi khi map Order ID {} sang DTO: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Lỗi khi lấy chi tiết đơn hàng: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrderHistoryForUser(Integer userId) {
        log.info("SERVICE - getOrderHistoryForUser - Fetching order history for User ID: {}", userId);
        if (userId == null || !userRepository.existsById(userId)) {
            throw new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId);
        }
        List<Order> orders = orderRepository.findByUserIdOrderByCreateAtDesc(userId);
        return orders.stream().map(ord -> {
            try {
                return orderMapper.toDTO(ord);
            } catch (Exception e) {
                log.error("SERVICE - getOrderHistoryForUser - Lỗi mapping Order ID {}: {}", ord.getId(), e.getMessage(), e);
                return null;
            }
        }).filter(java.util.Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByIdForAdmin(Integer orderId) {
        log.info("SERVICE - ADMIN - getOrderByIdForAdmin - Fetching order by ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));
        try {
            return orderMapper.toDTO(order);
        } catch (Exception e) {
            log.error("SERVICE - ADMIN - getOrderByIdForAdmin - Lỗi mapping Order ID {} to DTO: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Lỗi khi lấy chi tiết đơn hàng (admin): " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrdersForAdmin(Pageable pageable) {
        log.info("SERVICE - ADMIN - getAllOrdersForAdmin - Page: {}, Size: {}, Sort: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createAt").descending());
        }
        Page<Order> orderPage = orderRepository.findAll(pageable);
        try {
            return orderPage.map(orderMapper::toDTO);
        } catch (Exception e) {
            log.error("SERVICE - ADMIN - getAllOrdersForAdmin - Lỗi mapping Page<Order> to Page<OrderResponse>: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi lấy danh sách đơn hàng (admin): " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatusForAdmin(Integer orderId, String newStatus) {
        log.info("SERVICE - ADMIN - updateOrderStatusForAdmin - Order ID: {} to status: {}", orderId, newStatus);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));
        // TODO: Thêm validation cho newStatus
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order); // Không cần saveAndFlush ở đây trừ khi có lý do đặc biệt
        try {
            return orderMapper.toDTO(updatedOrder);
        } catch (Exception e) {
            log.error("SERVICE - ADMIN - updateOrderStatusForAdmin - Lỗi mapping Order ID {} to DTO: {}", updatedOrder.getId(), e.getMessage(), e);
            throw new RuntimeException("Lỗi khi cập nhật trạng thái đơn hàng: " + e.getMessage(), e);
        }
    }
}