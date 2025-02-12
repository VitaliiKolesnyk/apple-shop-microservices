package org.productservice.service;

import org.productservice.dto.category.CategoryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> findAll();

    CategoryResponse save(String name, MultipartFile thumbnail, String parentCategoryId);

    void deleteCategory(String categoryId);

    CategoryResponse findById(String categoryId);

    CategoryResponse updateCategory(String categoryId, String name, MultipartFile thumbnail);

    List<CategoryResponse> findSubcategories(String parentCategoryId);
}
