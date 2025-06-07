package com.BTLJAVA.WebBanThucPhamKho.service.impl;

import com.BTLJAVA.WebBanThucPhamKho.dto.request.OrderRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.CartItemResponse;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.CartResponse;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.OrderResponse;
import com.BTLJAVA.WebBanThucPhamKho.entity.*;
import com.BTLJAVA.WebBanThucPhamKho.mapper.AddressMapper;
import com.BTLJAVA.WebBanThucPhamKho.mapper.OrderMapper;
import com.BTLJAVA.WebBanThucPhamKho.repository.*;
import com.BTLJAVA.WebBanThucPhamKho.service.CartItemService;
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

import java.sql.SQLException;
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
    public static final String ORDER_STATUS_PROCESSING = "PROCESSING";
    public static final String ORDER_STATUS_SHIPPED = "SHIPPED";
    public static final String ORDER_STATUS_DELIVERED = "DELIVERED";
    public static final String ORDER_STATUS_CANCELLED = "CANCELLED";
    public static final String ORDER_STATUS_FAILED = "FAILED"; // Thêm trạng thái thất bại

    @Override
    @Transactional // Rất quan trọng!
    public OrderResponse placeOrder(Integer userId, OrderRequest orderRequest) {
        log.info("SERVICE::placeOrder - START - UserID: {}, GuestCartID: {}", userId, orderRequest.getGuestCartId());

        User userEntity = null;
        if (userId != null) {
            userEntity = userRepository.findById(userId) // userEntity có thể được lấy LAZY
                    .orElseThrow(() -> {
                        log.error("SERVICE::placeOrder - User not found with ID: {}", userId);
                        return new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId);
                    });
            log.info("SERVICE::placeOrder - User found: {}", userEntity.getUsername());
        } else {
            log.info("SERVICE::placeOrder - Placing order as guest. GuestCartID from request: {}", orderRequest.getGuestCartId());
        }

        // Lấy giỏ hàng thực tế. Logic này quan trọng.
        CartResponse cart = cartItemService.getCart(userId, orderRequest.getGuestCartId());
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            log.warn("SERVICE::placeOrder - Cart is empty. UserID: {}, GuestCartID: {}", userId, orderRequest.getGuestCartId());
            throw new IllegalStateException("Giỏ hàng trống, không thể đặt hàng.");
        }
        log.info("SERVICE::placeOrder - Cart retrieved: {} unique items, total quantity {}. Subtotal: {}",
                cart.getTotalUniqueItems(), cart.getTotalQuantity(), cart.getSubtotalPrice());

        // 1. Validate OrderRequest (cơ bản ở service, @Valid đã xử lý nhiều ở controller)
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
        if (shippingAddressEntity == null) { // Kiểm tra nếu mapper trả về null (không mong muốn)
            log.error("SERVICE::placeOrder - AddressMapper returned null for shippingAddressEntity. Check mapper logic.");
            throw new RuntimeException("Lỗi nội bộ: Không thể tạo thực thể địa chỉ từ yêu cầu.");
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
        Order order = Order.builder()
                .user(userEntity)
                .address(savedAddress)
                .title(orderRequest.getTitle())
                .customerName(orderRequest.getCustomerName())
                .customerPhone(orderRequest.getCustomerPhone())
                .customerEmail(orderRequest.getCustomerEmail())
                .status(ORDER_STATUS_PENDING) // Trạng thái mặc định
                .build();
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
            // Kiểm tra số lượng và giá cả
            if (cartItemDto.getQuantity() == null || cartItemDto.getQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng sản phẩm '" + (cartItemDto.getProductName() != null ? cartItemDto.getProductName() : "ID " + cartItemDto.getProductId()) + "' không hợp lệ (phải > 0).");
            }
            if (cartItemDto.getUnitPrice() == null || cartItemDto.getUnitPrice() < 0) {
                throw new IllegalArgumentException("Đơn giá sản phẩm '" + (cartItemDto.getProductName() != null ? cartItemDto.getProductName() : "ID " + cartItemDto.getProductId()) + "' không hợp lệ (phải >= 0).");
            }

            Product product = productRepository.findById(cartItemDto.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Sản phẩm '" + (cartItemDto.getProductName() != null ? cartItemDto.getProductName() : "ID " + cartItemDto.getProductId()) + "' không tồn tại trong hệ thống."));

            // Đảm bảo giá trong giỏ hàng khớp với giá hiện tại của sản phẩm (hoặc dùng giá giỏ hàng nếu muốn)
            if (!product.getPrice().equals(cartItemDto.getUnitPrice())) {
                log.warn("SERVICE::placeOrder - Giá sản phẩm {} thay đổi từ {} (trong giỏ) thành {} (hiện tại). Sử dụng giá hiện tại.",
                        product.getName(), cartItemDto.getUnitPrice(), product.getPrice());
                cartItemDto.setUnitPrice(product.getPrice()); // Cập nhật DTO với giá hiện tại của sản phẩm
            }

            if (product.getStockQuantity() == null || product.getStockQuantity() < cartItemDto.getQuantity()) {
                throw new IllegalStateException("Sản phẩm '" + product.getName() + "' không đủ số lượng tồn kho ("+ (product.getStockQuantity() != null ? product.getStockQuantity() : 0) +" còn lại, cần "+ cartItemDto.getQuantity() +").");
            }

            // Cập nhật tồn kho và số lượng đã bán
            product.setStockQuantity(product.getStockQuantity() - cartItemDto.getQuantity());
            product.setSoldQuantity((product.getSoldQuantity() != null ? product.getSoldQuantity() : 0) + cartItemDto.getQuantity());
            productRepository.save(product); // save thôi, flush sẽ xảy ra ở cuối transaction
            log.debug("SERVICE::placeOrder - Updated stock for product ID: {}. New stock: {}, New sold: {}", product.getId(), product.getStockQuantity(), product.getSoldQuantity());

            OrderDetail detail = OrderDetail.builder()
                    .order(order) // Sẽ được liên kết bởi Hibernate khi Order được save
                    .product(product)
                    .quantity(cartItemDto.getQuantity())
                    .price(cartItemDto.getUnitPrice()) // Sử dụng giá tại thời điểm đặt hàng
                    .status(ORDER_STATUS_PENDING) // Trạng thái cho từng chi tiết đơn hàng
                    .build();
            orderDetailsSet.add(detail);
            calculatedTotalPrice += (cartItemDto.getUnitPrice() * cartItemDto.getQuantity());
        }

        order.setOrderDetails(orderDetailsSet);
        order.setTotalPrice(calculatedTotalPrice);
        log.info("SERVICE::placeOrder - Order entity prepared with {} details. Calculated total price: {}", orderDetailsSet.size(), calculatedTotalPrice);

        // 5. Lưu Order (OrderDetails sẽ được lưu nhờ CascadeType.ALL trên orderDetailsSet)
        Order savedOrder;
        try {
            log.debug("SERVICE::placeOrder - Attempting to save Order. CustomerName: {}, TotalPrice: {}, Details count: {}",
                    order.getCustomerName(), order.getTotalPrice(), order.getOrderDetails().size());
            savedOrder = orderRepository.saveAndFlush(order); // saveAndFlush để đồng bộ ngay với DB
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

        // 6. Xóa giỏ hàng sau khi đặt hàng thành công
        try {
            cartItemService.clearCart(userId, orderRequest.getGuestCartId());
            log.info("SERVICE::placeOrder - Cart cleared for UserID: {} / GuestCartID: {}", userId, orderRequest.getGuestCartId());
        } catch (Exception e) {
            log.error("SERVICE::placeOrder - Error clearing cart after order placement. UserID: {}, GuestCartID: {}. Error: {}. Order (ID: {}) was still created, manual cleanup may be needed.",
                    userId, orderRequest.getGuestCartId(), e.getMessage(), savedOrder.getId(), e);
            // Không ném lại lỗi này để tránh rollback đơn hàng đã thành công, chỉ log.
        }

        // 7. Map sang DTO để trả về. Đảm bảo OrderMapper xử lý các mối quan hệ LAZY đã được tải EAGER trong Order.java
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


    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByIdForUser(Integer orderId, Integer userId) {
        log.info("SERVICE - getOrderByIdForUser - Fetching order ID: {} for User ID: {}", orderId, userId);
        if (userId == null) {
            log.warn("SERVICE - getOrderByIdForUser - UserID không được null.");
            throw new IllegalArgumentException("UserID là bắt buộc để xem đơn hàng.");
        }
        // Để đảm bảo load đúng đơn hàng của user đó
        Order order = orderRepository.findById(orderId) // findById với EntityGraph từ OrderRepository
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        // Kiểm tra quyền sở hữu đơn hàng
        if (order.getUser() != null) { // Đơn hàng của người dùng đã đăng nhập
            if (!order.getUser().getId().equals(userId)) {
                log.warn("SERVICE - User {} cố gắng truy cập đơn hàng {} không thuộc về họ.", userId, orderId);
                throw new AccessDeniedException("Bạn không có quyền xem đơn hàng này.");
            }
        } else { // Đơn hàng của khách vãng lai (order.getUser() == null)
            // Nếu người dùng hiện tại đã đăng nhập (userId != null), nhưng lại cố gắng xem đơn hàng của khách
            // (order.getUser() == null), thì từ chối. Nếu bạn muốn cho phép khách xem lại đơn hàng của họ
            // mà không cần đăng nhập, bạn cần một cơ chế xác thực guestCartId ở đây.
            log.warn("SERVICE - Người dùng đã xác thực {} cố gắng truy cập đơn hàng của khách {}. Từ chối.", userId, orderId);
            throw new AccessDeniedException("Không thể truy cập đơn hàng của khách từ tài khoản đăng nhập.");
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
        if (userId == null) {
            log.warn("SERVICE - getOrderHistoryForUser - UserID is null, cannot fetch history.");
            throw new IllegalArgumentException("UserID là bắt buộc để lấy lịch sử đơn hàng.");
        }
        if (!userRepository.existsById(userId)) { // Kiểm tra sự tồn tại của user
            throw new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId);
        }
        List<Order> orders = orderRepository.findByUserIdOrderByCreateAtDesc(userId); // Sử dụng phương thức của Repository với EntityGraph
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
        Order order = orderRepository.findById(orderId) // findById với EntityGraph từ OrderRepository
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
        // Sử dụng findAll với EntityGraph từ OrderRepository
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

        if (!isValidOrderStatus(newStatus)) {
            throw new IllegalArgumentException("Trạng thái đơn hàng '" + newStatus + "' không hợp lệ.");
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        try {
            return orderMapper.toDTO(updatedOrder);
        } catch (Exception e) {
            log.error("SERVICE - ADMIN - updateOrderStatusForAdmin - Lỗi mapping Order ID {} to DTO: {}", updatedOrder.getId(), e.getMessage(), e);
            throw new RuntimeException("Lỗi khi cập nhật trạng thái đơn hàng: " + e.getMessage(), e);
        }
    }

    private boolean isValidOrderStatus(String status) {
        return status.equals(ORDER_STATUS_PENDING) ||
                status.equals(ORDER_STATUS_PROCESSING) ||
                status.equals(ORDER_STATUS_SHIPPED) ||
                status.equals(ORDER_STATUS_DELIVERED) ||
                status.equals(ORDER_STATUS_CANCELLED) ||
                status.equals(ORDER_STATUS_FAILED);
    }
}