package com.BTLJAVA.WebBanThucPhamKho.service.impl;

import com.BTLJAVA.WebBanThucPhamKho.dto.request.ProductRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.ProductResponse;
import com.BTLJAVA.WebBanThucPhamKho.entity.Category;
import com.BTLJAVA.WebBanThucPhamKho.entity.Product;
import com.BTLJAVA.WebBanThucPhamKho.mapper.ProductMapper;
import com.BTLJAVA.WebBanThucPhamKho.repository.CategoryRepository;
import com.BTLJAVA.WebBanThucPhamKho.repository.ProductRepository;
import com.BTLJAVA.WebBanThucPhamKho.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    public List<ProductResponse> getProducts(Integer pageNumber, Integer size) {
        Pageable pageable = PageRequest.of(pageNumber, size);
        List<Product> products = productRepository.findAll(pageable).getContent();

        return products.stream().map(productMapper::toDTO).toList();
    }

    @Override
    public ProductResponse createProduct(ProductRequest productRequest) {
        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not exist"));

        Product product = Product.builder()
                .name(productRequest.getName())
                .category(category)
                .imageUrl(productRequest.getImageUrl())
                .price(productRequest.getPrice())
                .stockQuantity(productRequest.getStockQuantity())
                .soldQuantity(0)
                .expiryDate(productRequest.getExpiryDate())
                .manufactureDate(productRequest.getManufactureDate())// mới tạo nên chưa bán
                .build();

        product = productRepository.save(product);

        return productMapper.toDTO(product);
    }

    @Override
    public ProductResponse updateProduct(Integer id, ProductRequest productRequest) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not exist"));

        product.setName(productRequest.getName());
        product.setCategory(category);
        product.setImageUrl(productRequest.getImageUrl());
        product.setPrice(productRequest.getPrice());
        product.setStockQuantity(productRequest.getStockQuantity());


        product = productRepository.save(product);

        return productMapper.toDTO(product);
    }

    @Override
    public void deleteProduct(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        productRepository.delete(product);
    }

    @Override
    public ProductResponse getProductById(Integer id) {
        Optional<Product> productOptional = productRepository.findById(id);
        return productOptional.map(productMapper::toDTO).orElse(null);
    }
}
