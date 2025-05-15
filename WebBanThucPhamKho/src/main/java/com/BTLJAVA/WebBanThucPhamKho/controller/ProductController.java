package com.BTLJAVA.WebBanThucPhamKho.controller;

import com.BTLJAVA.WebBanThucPhamKho.dto.request.ProductRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.ProductResponse;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.ResponseData;
import com.BTLJAVA.WebBanThucPhamKho.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
@EnableMethodSecurity
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseData<List<ProductResponse>> getProducts(
            @RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {

        List<ProductResponse> products = productService.getProducts(pageNumber, pageSize);

        return ResponseData.<List<ProductResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(products)
                .build();
    }

 @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseData<ProductResponse> createProduct(@RequestBody ProductRequest productRequest) {
        ProductResponse product = productService.createProduct(productRequest);

        return ResponseData.<ProductResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Success")
                .data(product)
                .build();
    }

 @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseData<ProductResponse> updateProduct(
            @PathVariable("id") Integer id,
            @RequestBody ProductRequest productRequest) {

        ProductResponse product = productService.updateProduct(id, productRequest);

        return ResponseData.<ProductResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(product)
                .build();
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseData<String> deleteProduct(@PathVariable("id") Integer id) {
        productService.deleteProduct(id);
        return ResponseData.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Deleted successfully")
                .data("Product with ID " + id + " has been deleted")
                .build();
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseData<ProductResponse> getProductById(@PathVariable("id") Integer id) {
        // Bạn sẽ cần tạo phương thức getProductById trong ProductService
        ProductResponse product = productService.getProductById(id); // Ví dụ
        if (product == null) {
            return ResponseData.<ProductResponse>builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message("Product not found with ID " + id)
                    .build();
        }
        return ResponseData.<ProductResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(product)
                .build();
    }

}
