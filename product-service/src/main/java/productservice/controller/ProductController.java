package productservice.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.productservice.dto.product.ProductResponse;
import org.productservice.mapper.ProductMapper;
import org.productservice.service.FileService;
import org.productservice.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    private final ProductMapper productMapper;

    private final ObjectMapper objectMapper;

    private final FileService fileService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@RequestParam("skuCode") String skuCode,
                                         @RequestParam("name") String name,
                                         @RequestParam("description") String description,
                                         @RequestParam("price") double price,
                                         @RequestParam("isExclusive") boolean isExclusive,
                                         @RequestParam("categories") String categoriesJson,
                                         @RequestParam("thumbnail") MultipartFile thumbnail) throws JsonProcessingException, ExecutionException, InterruptedException {
        List<String> categories = objectMapper.readValue(categoriesJson, new TypeReference<List<String>>() {});

        return productService.createProduct(skuCode, name, description, price, categories, thumbnail, isExclusive);
    }

    @GetMapping("/paged")
    @ResponseStatus(HttpStatus.OK)
    public Page<ProductResponse> getAllProducts(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(defaultValue = "id") String sortBy,
                                                @RequestParam(defaultValue = "ASC") String sortDir,
                                                @RequestParam(defaultValue = "all") String categoryId,
                                                @RequestParam(defaultValue = "") String search) {
        Sort sort = Sort.by(Sort.Order.by(sortBy).with(Sort.Direction.fromString(sortDir)));
        Pageable pageable = PageRequest.of(page, size, sort);

        return productService.findAll(categoryId, search, pageable);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteProduct(@PathVariable String productId) throws ExecutionException, InterruptedException {
        productService.delete(productId);
    }

    @PostMapping("/thumbnail")
    @ResponseStatus(HttpStatus.CREATED)
    public String uploadThumbnail(@RequestParam("file") MultipartFile file, @RequestParam("id") String id) {
        return productService.uploadThumbnail(file, id);
    }

    @GetMapping("/{productId}")
    public ProductResponse getProductById(@PathVariable String productId) {
        return productMapper.mapToProductResponse(productService.getProductById(productId));
    }

    @PutMapping("/{productId}")
    public ProductResponse updateProduct(@PathVariable String productId,
                                         @RequestParam(value = "name", required = false) String name,
                                         @RequestParam(value = "description", required = false) String description,
                                         @RequestParam(value = "price", required = false) double price,
                                         @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail) throws ExecutionException, InterruptedException {
        return productService.updateProduct(productId, name, description, price, thumbnail);
    }
}
