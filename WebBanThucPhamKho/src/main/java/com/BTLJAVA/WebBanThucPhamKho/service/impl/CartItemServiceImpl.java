package com.BTLJAVA.WebBanThucPhamKho.service.impl;

import com.BTLJAVA.WebBanThucPhamKho.dto.request.CartItemRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.request.CartItemUpdateQuantityRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.CartItemResponse;
import com.BTLJAVA.WebBanThucPhamKho.entity.CartItem;
import com.BTLJAVA.WebBanThucPhamKho.entity.Product;
import com.BTLJAVA.WebBanThucPhamKho.entity.User;
import com.BTLJAVA.WebBanThucPhamKho.mapper.CartItemMapper;
import com.BTLJAVA.WebBanThucPhamKho.repository.CartItemRepository;
import com.BTLJAVA.WebBanThucPhamKho.repository.ProductRepository;
import com.BTLJAVA.WebBanThucPhamKho.repository.UserRepository;
import com.BTLJAVA.WebBanThucPhamKho.service.CartItemService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartItemMapper cartItemMapper;

    @Override
    public List<CartItemResponse> getCartByUserId(Integer userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        return cartItems.stream().map(cartItemMapper::toDTO).toList();
    }

    @Override
    public CartItemResponse createCartItem(Integer userId, CartItemRequest cartItemRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Product product = productRepository.findById(cartItemRequest.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        if (product.getStockQuantity() < cartItemRequest.getQuantity()) {
            throw new RuntimeException("The product " + product.getName() + " is out of stock");
        }

        CartItem cartItem = CartItem.builder()
                .user(user)
                .product(product)
                .quantity(cartItemRequest.getQuantity())
                .price(product.getPrice() * cartItemRequest.getQuantity())
                .build();

        cartItem = cartItemRepository.save(cartItem);
        log.info("Created cartItemId {} by userId {}", cartItem.getId(), userId);

        return cartItemMapper.toDTO(cartItem);
    }

    @Override
    public CartItemResponse updateCartItem(Integer userId, Integer id, CartItemUpdateQuantityRequest cartItemRequest) {
        CartItem cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CartItem not found"));

        if (!cartItem.getUser().getId().equals(userId)) {
            throw new RuntimeException("Cannot update this cart item");
        }

        Product product = cartItem.getProduct();

        if (product.getStockQuantity() < cartItemRequest.getQuantity()) {
            throw new RuntimeException("The product " + product.getName() + " is out of stock");
        }

        cartItem.setQuantity(cartItemRequest.getQuantity());
        cartItem.setPrice(product.getPrice() * cartItemRequest.getQuantity());

        cartItem = cartItemRepository.save(cartItem);
        log.info("Updated cartItemId {} by userId {}", cartItem.getId(), userId);

        return cartItemMapper.toDTO(cartItem);
    }

    @Override
    public void deleteCartItem(Integer userId, Integer cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found"));

        if (!cartItem.getUser().getId().equals(userId)) {
            throw new RuntimeException("Cannot delete this cart item");
        }

        cartItemRepository.deleteById(cartItemId);
        log.info("Deleted cart item {}", cartItemId);
    }
}
