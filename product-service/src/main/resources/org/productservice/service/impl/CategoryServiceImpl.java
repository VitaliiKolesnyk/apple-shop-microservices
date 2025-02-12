package org.productservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.productservice.dto.category.CategoryResponse;
import org.productservice.entity.Category;
import org.productservice.repository.CategoryRepository;
import org.productservice.service.CategoryService;
import org.productservice.service.FileService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final FileService fileService;

    @Override
    @Cacheable(value = "categories")
    public List<CategoryResponse> findAll() {
        log.info("Fetching all categories from the database.");

        List<CategoryResponse> categories = categoryRepository.findAll()
                .stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());

        log.info("Fetched {} categories", categories.size());

        return categories;
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse save(String name, MultipartFile thumbnail, String parentCategoryId) {
        log.info("Saving new category with name: {} and parentCategoryId: {}", name, parentCategoryId);

        Category subcategory = new Category();
        subcategory.setName(name);

        if (thumbnail != null) {
            log.info("Thumbnail is - " + thumbnail.getOriginalFilename());
            subcategory.setThumbnailUrl(fileService.uploadFile(thumbnail));
        }

        subcategory.setId(UUID.randomUUID().toString());  // Set ID for the subcategory

        // If the parentCategoryId is provided, attach to a parent category
        if (parentCategoryId != null) {
            // Fetch all categories to find the correct parent category
            List<Category> categories = categoryRepository.findAll();

            // Find the top parent category in the tree
            Category topParentCategory = findTopParentCategory(categories, parentCategoryId);

            if (topParentCategory == null) {
                log.error("Top parent category with ID {} not found", parentCategoryId);
                throw new RuntimeException("Parent category not found");
            }

            // Add the subcategory to the parent category’s subcategories list
            addSubcategoryToParent(topParentCategory, subcategory, parentCategoryId);

            // Save the top-level parent category, which includes all subcategories
            categoryRepository.save(topParentCategory);

            log.info("Successfully added subcategory to parent category with ID: {}", topParentCategory.getId());

            return mapToCategoryResponse(topParentCategory);
        } else {
            // If no parent category, this is a main category
            subcategory.setMain(true);
            categoryRepository.save(subcategory);

            return mapToCategoryResponse(subcategory);
        }
    }

    private Category findTopParentCategory(List<Category> categories, String categoryId) {
        for (Category category : categories) {
            if (category.getId() == null) continue;

            // Check if the category is the one we are looking for
            if (category.getId().equals(categoryId)) {
                return category;
            }

            // If the category has subcategories, look for the category within them
            if (!category.getSubcategories().isEmpty()) {
                Category found = findCategoryInTree(category.getSubcategories(), categoryId);
                if (found != null) {
                    return category; // Return the top-level parent
                }
            }
        }
        return null;
    }

    private boolean addSubcategoryToParent(Category parentCategory, Category subcategory, String parentCategoryId) {
        if (parentCategory.getId().equals(parentCategoryId)) {
            parentCategory.getSubcategories().add(subcategory);
            return true;
        }

        // Check if the current category's subcategory matches the parentCategoryId
        for (Category childCategory : parentCategory.getSubcategories()) {
            if (childCategory.getId().equals(parentCategoryId)) {
                // Add the subcategory to the child category's subcategories list
                childCategory.getSubcategories().add(subcategory);
                log.info("Subcategory {} added to parent category with ID {}", subcategory.getName(), parentCategoryId);
                return true; // Successfully added the subcategory
            }
        }

        // Recursively search the subcategories of each child category
        for (Category childCategory : parentCategory.getSubcategories()) {
            boolean isAdded = addSubcategoryToParent(childCategory, subcategory, parentCategoryId);
            if (isAdded) {
                return true;  // If subcategory was added in recursion, stop further search
            }
        }

        return false; // Return false if parentCategoryId was not found
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(String categoryId) {
        log.info("Deleting category with ID: {}", categoryId);

        // First, attempt to find the category in the repository directly (top-level category)
        Category category = categoryRepository.findById(categoryId).orElse(null);

        if (category != null) {
            if (category.isMain()) {
                // Top-level category, delete directly
                categoryRepository.delete(category);
                fileService.deleteFile(category.getThumbnailUrl());
                log.info("Successfully deleted top-level category with ID: {}", categoryId);
            }
        } else {
            // If not found as a top-level category, search through all categories for the subcategory
            List<Category> categories = categoryRepository.findAll();
            Category topParentCategory = findTopParentCategory(categories, categoryId);

            boolean removed = removeSubcategoryRecursive(topParentCategory, categoryId);

            if (!removed) {
                log.error("Category with ID {} not found in any parent category", categoryId);
                throw new RuntimeException("Category not found");
            } else {
                categoryRepository.save(topParentCategory);
            }

            log.info("Successfully removed subcategory with ID: {} from parent", categoryId);
        }
    }

    private boolean removeSubcategoryRecursive(Category parentCategory, String subcategoryId) {
        // Look for the subcategory in the parent's subcategories
        for (Category subcategory : parentCategory.getSubcategories()) {
            if (subcategory.getId().equals(subcategoryId)) {
                // Remove the subcategory and its thumbnail file
                log.info("Before deletion - " + parentCategory.getSubcategories().size());

                parentCategory.getSubcategories().remove(subcategory);

                log.info("After deletion - " + parentCategory.getSubcategories().size());

                fileService.deleteFile(subcategory.getThumbnailUrl());
                return true;  // Successfully removed
            }
        }

        // If not found, check the subcategories of each child category
        for (Category subcategory : parentCategory.getSubcategories()) {
            if (removeSubcategoryRecursive(subcategory, subcategoryId)) {
                return true;  // Subcategory found and removed in recursion
            }
        }

        return false;  // Subcategory not found in this branch of the tree
    }

    @Override
    public CategoryResponse findById(String categoryId) {
        log.info("Fetching category with ID: {}", categoryId);

        List<Category> categories = categoryRepository.findAll();

        Category category = findCategoryInTree(categories, categoryId);

        if (category == null) {
            log.error("Category with ID {} not found", categoryId);
            throw new RuntimeException("Category not found");
        }

        log.info("Fetched category with ID: {} and name: {}", categoryId, category.getName());

        return mapToCategoryResponse(category);
    }

    private Category findCategoryInTree(List<Category> categories, String categoryId) {
        for (Category category : categories) {

            if (category.getId() == null) {
                continue;
            }

            if (category.getId().equals(categoryId)) {
                return category;
            }

            if (!category.getSubcategories().isEmpty()) {
                Category found = findCategoryInTree(category.getSubcategories(), categoryId);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse updateCategory(String categoryId, String name, MultipartFile thumbnail) {
        log.info("Updating category with ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.error("Category with ID {} not found", categoryId);
                    return new RuntimeException("Category not found");
                });

        category.setName(name);

        if (thumbnail != null) {
            log.info("Updating thumbnail for category with ID: {}", categoryId);
            fileService.deleteFile(category.getThumbnailUrl());
            category.setThumbnailUrl(fileService.uploadFile(thumbnail));
        }

        Category result = categoryRepository.save(category);

        log.info("Successfully updated category with ID: {} and name: {}", category.getId(), category.getName());

        return mapToCategoryResponse(result);
    }

    @Override
    public List<CategoryResponse> findSubcategories(String parentCategoryId) {
        log.info("Fetching subcategories for parent category ID: {}", parentCategoryId);

        List<CategoryResponse> subcategories = categoryRepository.findByParentCategoryId(parentCategoryId)
                .stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());

        log.info("Fetched {} subcategories for parent category ID: {}", subcategories.size(), parentCategoryId);

        return subcategories;
    }

    private CategoryResponse mapToCategoryResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getThumbnailUrl(),
                category.isMain(),
                category.getSubcategories().stream()
                        .map(this::mapToCategoryResponse)
                        .collect(Collectors.toList())
        );
    }
}
