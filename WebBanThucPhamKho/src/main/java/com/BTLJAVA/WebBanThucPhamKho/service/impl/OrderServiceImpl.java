package com.BTLJAVA.WebBanThucPhamKho.service.impl;

import com.BTLJAVA.WebBanThucPhamKho.dto.request.AddressRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.request.OrderStatusUpdateRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.OrderResponse;
import com.BTLJAVA.WebBanThucPhamKho.entity.*;
import com.BTLJAVA.WebBanThucPhamKho.mapper.AddressMapper;
import com.BTLJAVA.WebBanThucPhamKho.repository.*;
import com.BTLJAVA.WebBanThucPhamKho.service.OrderService;
import com.BTLJAVA.WebBanThucPhamKho.util.TypeStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;
    private final CartItemRepository cartItemRepository;

    @Override
    public List<OrderResponse> getOrders(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<Order> orders = orderRepository.findAll(pageable).getContent();

        return orders.stream().map(order -> OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .address(addressMapper.toDTO(order.getAddress()))
                .price(order.getTotalPrice())
                .status(order.getStatus())
                .build()).toList();
    }

    @Override
    public List<OrderResponse> getOrdersByUserId(Integer userId, Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<Order> orders = orderRepository.findByUserId(userId, pageable);

        return orders.stream().map(order -> OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .address(addressMapper.toDTO(order.getAddress()))
                .price(order.getTotalPrice())
                .status(order.getStatus())
                .build()).toList();
    }

    @Override
    public OrderResponse createOrder(Integer userId, AddressRequest addressRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Address address = addressRepository.save(addressMapper.toEntity(addressRequest));

        Order order = Order.builder()
                .user(user)
                .status(TypeStatus.PENDING.name())
                .address(address)
                .build();

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);

        List<Integer> productIds = cartItems.stream()
                .map(cartItem -> cartItem.getProduct().getId())
                .toList();

        Map<Integer, Integer> cart = new HashMap<>();
        cartItems.forEach(cartItem -> cart.put(cartItem.getProduct().getId(), cartItem.getQuantity()));

        List<Product> products = productRepository.findAllById(productIds);
        Set<OrderDetail> orderDetails = new HashSet<>();

        StringJoiner title = new StringJoiner(", ");
        int totalPrice = 0;

        for (Product product : products) {
            Integer stock = product.getStockQuantity();
            Integer quantity = cart.get(product.getId());

            if (stock >= quantity) {
                product.setStockQuantity(stock - quantity);
                product.setSoldQuantity(product.getSoldQuantity() + quantity);

                title.add(product.getName());
                totalPrice += product.getPrice() * quantity;

                orderDetails.add(OrderDetail.builder()
                        .order(order)
                        .product(product)
                        .quantity(quantity)
                        .price(product.getPrice() * quantity)
                        .status(TypeStatus.PENDING.name())
                        .build());
            } else {
                throw new RuntimeException("The product " + product.getName() + " is out of stock");
            }
        }

        order.setTitle(title.toString());
        order.setTotalPrice(totalPrice);
        order.setOrderDetails(orderDetails);

        productRepository.saveAll(products);
        cartItemRepository.deleteAll(cartItems);
        order = orderRepository.save(order);

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .address(addressMapper.toDTO(order.getAddress()))
                .price(order.getTotalPrice())
                .status(order.getStatus())
                .build();
    }

    @Override
    public OrderResponse updateStatusOder(Integer id, OrderStatusUpdateRequest orderStatusUpdateRequest) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        order.setStatus(orderStatusUpdateRequest.getStatus().toUpperCase());
        order = orderRepository.save(order);

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .address(addressMapper.toDTO(order.getAddress()))
                .price(order.getTotalPrice())
                .status(order.getStatus())
                .build();
    }
}
