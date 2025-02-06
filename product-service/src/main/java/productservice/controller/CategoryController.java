package productservice.controller;

import lombok.RequiredArgsConstructor;
import org.productservice.dto.category.CategoryResponse;
import org.productservice.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse save(@RequestParam("name") String name,
                         @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
                         @RequestParam(required = false) String parentCategoryId
    ) {
        return categoryService.save(name, thumbnail, parentCategoryId);
    }

    @GetMapping
    public List<CategoryResponse> findAll() {
        return categoryService.findAll();
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteCategory(@PathVariable String categoryId) {
        categoryService.deleteCategory(categoryId);
    }

    @PutMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateCategory(@PathVariable String categoryId,
                               @RequestParam(name = "name", required = false) String name,
                               @RequestParam(name = "thumbnail", required = false) MultipartFile thumbnail
    ) {
        categoryService.updateCategory(categoryId, name, thumbnail);
    }

    @GetMapping("/{categoryId}")
    public CategoryResponse getCategoryById(@PathVariable String categoryId) {
        return categoryService.findById(categoryId);
    }

    @GetMapping("/{parentCategoryId}/subcategories")
    public List<CategoryResponse> getSubcategories(@PathVariable String parentCategoryId) {
        return categoryService.findSubcategories(parentCategoryId);
    }
}
