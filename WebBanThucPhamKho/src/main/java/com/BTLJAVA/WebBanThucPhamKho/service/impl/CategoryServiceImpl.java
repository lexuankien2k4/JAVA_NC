package com.BTLJAVA.WebBanThucPhamKho.service.impl;

import com.BTLJAVA.WebBanThucPhamKho.dto.request.CategoryRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.CategoryResponse;
import com.BTLJAVA.WebBanThucPhamKho.entity.Category;
import com.BTLJAVA.WebBanThucPhamKho.mapper.CategoryMapper;
import com.BTLJAVA.WebBanThucPhamKho.repository.CategoryRepository;
import com.BTLJAVA.WebBanThucPhamKho.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        log.info("Fetching all categories");
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Integer id) {
        log.info("Fetching category by ID: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Category not found with ID: {}", id);
                    return new EntityNotFoundException("Không tìm thấy danh mục với ID: " + id);
                });
        return categoryMapper.toDTO(category);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        log.info("Creating new category with name: {}", categoryRequest.getName());
        if (categoryRepository.existsByName(categoryRequest.getName())) {
            log.warn("Category with name '{}' already exists.", categoryRequest.getName());
            throw new IllegalArgumentException("Tên danh mục đã tồn tại: " + categoryRequest.getName());
        }

        Category category = new Category();
        category.setName(categoryRequest.getName());

        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", savedCategory.getId());
        return categoryMapper.toDTO(savedCategory);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Integer id, CategoryRequest categoryRequest) {
        log.info("Updating category with ID: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy danh mục với ID: " + id));

        if (!category.getName().equals(categoryRequest.getName()) && categoryRepository.existsByName(categoryRequest.getName())) {
            throw new IllegalArgumentException("Tên danh mục mới đã tồn tại: " + categoryRequest.getName());
        }
        category.setName(categoryRequest.getName());

        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully with ID: {}", updatedCategory.getId());
        return categoryMapper.toDTO(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Integer id) {
        log.info("Deleting category with ID: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy danh mục với ID: " + id));

        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new IllegalStateException("Không thể xóa danh mục vì nó còn chứa sản phẩm.");
        }

        categoryRepository.delete(category);
        log.info("Category deleted successfully with ID: {}", id);
    }

}
