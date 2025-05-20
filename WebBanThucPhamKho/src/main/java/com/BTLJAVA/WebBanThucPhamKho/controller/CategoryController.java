package com.BTLJAVA.WebBanThucPhamKho.controller;

import com.BTLJAVA.WebBanThucPhamKho.dto.request.CategoryRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.CategoryResponse;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.ResponseData;
import com.BTLJAVA.WebBanThucPhamKho.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseData<List<CategoryResponse>> getAllCategories() {
        log.info("API GET /api/v1/categories - getAllCategories called");
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseData.<List<CategoryResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách danh mục thành công.")
                .data(categories)
                .build();
    }

    @GetMapping("/{id}")
    public ResponseData<CategoryResponse> getCategoryById(@PathVariable Integer id) {
        log.info("API GET /api/v1/categories/{} - getCategoryById called", id);
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseData.<CategoryResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy thông tin danh mục thành công.")
                .data(category)
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseData<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest categoryRequest) {
        log.info("API POST /api/v1/categories - createCategory called with name: {}", categoryRequest.getName());
        CategoryResponse category = categoryService.createCategory(categoryRequest);
        return ResponseData.<CategoryResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Tạo danh mục thành công.")
                .data(category)
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseData<CategoryResponse> updateCategory(@PathVariable Integer id, @Valid @RequestBody CategoryRequest categoryRequest) {
        log.info("API PUT /api/v1/categories/{} - updateCategory called", id);
        CategoryResponse category = categoryService.updateCategory(id, categoryRequest);
        return ResponseData.<CategoryResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật danh mục thành công.")
                .data(category)
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseData<String> deleteCategory(@PathVariable Integer id) {
        log.info("API DELETE /api/v1/categories/{} - deleteCategory called", id);
        categoryService.deleteCategory(id);
        return ResponseData.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Xóa danh mục thành công.")
                .data("Danh mục với ID " + id + " đã được xóa.")
                .build();
    }
}