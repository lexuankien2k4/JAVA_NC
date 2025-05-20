package com.BTLJAVA.WebBanThucPhamKho.service;

import com.BTLJAVA.WebBanThucPhamKho.dto.request.CategoryRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllCategories();
    CategoryResponse getCategoryById(Integer id);
    CategoryResponse createCategory(CategoryRequest categoryRequest);
    CategoryResponse updateCategory(Integer id, CategoryRequest categoryRequest);
    void deleteCategory(Integer id);
}
