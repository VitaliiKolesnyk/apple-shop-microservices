package org.productservice.service;



import org.productservice.dto.product.ProductResponse;
import org.productservice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ProductService {

    ProductResponse createProduct(String skuCode, String name, String description, double price, List<String> subcategories, MultipartFile thumbnail, boolean isExclusive) throws ExecutionException, InterruptedException;

    Page<ProductResponse> findAll(String categoryId, String search, Pageable pageable);

    void delete(String id) throws ExecutionException, InterruptedException;

    String uploadThumbnail(MultipartFile file, String id);

    Product getProductById(String id);

    ProductResponse updateProduct(String id, String name, String description, double price, MultipartFile thumbnail) throws ExecutionException, InterruptedException;
}
