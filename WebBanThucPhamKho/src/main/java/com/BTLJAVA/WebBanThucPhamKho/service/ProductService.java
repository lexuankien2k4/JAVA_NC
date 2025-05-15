package com.BTLJAVA.WebBanThucPhamKho.service;


import com.BTLJAVA.WebBanThucPhamKho.dto.request.ProductRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {
    List<ProductResponse> getProducts(Integer pageNumber, Integer size);

    ProductResponse createProduct(ProductRequest productRequest);

    ProductResponse updateProduct(Integer id, ProductRequest productRequest);
    void deleteProduct(Integer id);

    ProductResponse getProductById(Integer id);
}
