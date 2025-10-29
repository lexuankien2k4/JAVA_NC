package com.BTLJAVA.WebBanThucPhamKho.service.impl;

import com.BTLJAVA.WebBanThucPhamKho.dto.request.AddressRequest;
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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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
    public static final String ORDER_STATUS_FAILED = "FAILED";

    @Override
    @Transactional
    public OrderResponse placeOrder(Integer userId, OrderRequest orderRequest) {
        // 1. Lấy thông tin User (nếu có)
        User userEntity = (userId != null) ? userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId)) : null;

        // 2. Lấy giỏ hàng và kiểm tra
        CartResponse cart = cartItemService.getCart(userId, orderRequest.getGuestCartId());
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Giỏ hàng trống, không thể đặt hàng.");
        }

        validateOrderRequest(orderRequest);


        Address savedAddress = saveShippingAddress(orderRequest.getShippingAddress());

        Order order = Order.builder()
                .user(userEntity)
                .address(savedAddress)
                .title(orderRequest.getTitle())
                .customerName(orderRequest.getCustomerName())
                .customerPhone(orderRequest.getCustomerPhone())
                .customerEmail(orderRequest.getCustomerEmail())
                .status(ORDER_STATUS_PENDING)
                .build();

        OrderProcessingResult processingResult = processOrderDetailsAndUpdateStock(order, cart.getItems());

        order.setOrderDetails(processingResult.getOrderDetails());
        order.setTotalPrice(processingResult.getTotalPrice());


        Order savedOrder = orderRepository.saveAndFlush(order);

        cartItemService.clearCart(userId, orderRequest.getGuestCartId());
        return orderMapper.toDTO(savedOrder);
    }

    // --- Helper cho placeOrder ---

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    private static class OrderProcessingResult {
        private final Set<OrderDetail> orderDetails;
        private final int totalPrice;
    }

    private OrderProcessingResult processOrderDetailsAndUpdateStock(Order order, List<CartItemResponse> cartItems) {
        Set<OrderDetail> orderDetailsSet = new HashSet<>();
        List<Product> productsToUpdate = new ArrayList<>();
        int calculatedTotalPrice = 0;

        for (CartItemResponse cartItemDto : cartItems) {
            Product product = productRepository.findById(cartItemDto.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Sản phẩm '" + (cartItemDto.getProductName() != null ? cartItemDto.getProductName() : "ID " + cartItemDto.getProductId()) + "' không tồn tại."));

            // Sử dụng giá hiện tại của sản phẩm
            int currentPrice = product.getPrice();
            int quantity = cartItemDto.getQuantity();

            if (product.getStockQuantity() == null || product.getStockQuantity() < quantity) {
                throw new IllegalStateException("Sản phẩm '" + product.getName() + "' không đủ số lượng tồn kho.");
            }

            // Cập nhật tồn kho và số lượng đã bán
            product.setStockQuantity(product.getStockQuantity() - quantity);
            product.setSoldQuantity((product.getSoldQuantity() != null ? product.getSoldQuantity() : 0) + quantity);
            productsToUpdate.add(product);

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .quantity(quantity)
                    .price(currentPrice) // Sử dụng giá hiện tại
                    .status(ORDER_STATUS_PENDING)
                    .build();

            orderDetailsSet.add(detail);
            calculatedTotalPrice += (currentPrice * quantity);
        }

        // Lưu tất cả sản phẩm đã bị thay đổi tồn kho
        productRepository.saveAll(productsToUpdate);

        return new OrderProcessingResult(orderDetailsSet, calculatedTotalPrice);
    }

    private void validateOrderRequest(OrderRequest orderRequest) {
        if (orderRequest.getShippingAddress() == null) {
            throw new IllegalArgumentException("Thông tin địa chỉ giao hàng không được để trống.");
        }
        if (orderRequest.getCustomerName() == null || orderRequest.getCustomerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên khách hàng không được để trống.");
        }
        if (orderRequest.getCustomerPhone() == null || orderRequest.getCustomerPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại khách hàng không được để trống.");
        }
    }

    private Address saveShippingAddress(AddressRequest addressRequest) {
        Address shippingAddressEntity = addressMapper.requestToEntity(addressRequest);
        if (shippingAddressEntity == null) {
            throw new RuntimeException("Lỗi nội bộ: Không thể tạo thực thể địa chỉ từ yêu cầu.");
        }
        return addressRepository.saveAndFlush(shippingAddressEntity);
    }


    // --- Các phương thức khác ---

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByIdForUser(Integer orderId, Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("UserID là bắt buộc để xem đơn hàng.");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        // Kiểm tra quyền sở hữu
        if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Bạn không có quyền xem đơn hàng này.");
        }

        return orderMapper.toDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrderHistoryForUser(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("UserID là bắt buộc để lấy lịch sử đơn hàng.");
        }
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId);
        }

        List<Order> orders = orderRepository.findByUserIdOrderByCreateAtDesc(userId);

        // Đơn giản hóa stream, bỏ try-catch
        return orders.stream()
                .map(orderMapper::toDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByIdForAdmin(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));
        return orderMapper.toDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrdersForAdmin(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createAt").descending());
        }
        Page<Order> orderPage = orderRepository.findAll(pageable);
        return orderPage.map(orderMapper::toDTO);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatusForAdmin(Integer orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        if (!isValidOrderStatus(newStatus)) {
            throw new IllegalArgumentException("Trạng thái đơn hàng '" + newStatus + "' không hợp lệ.");
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toDTO(updatedOrder);
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