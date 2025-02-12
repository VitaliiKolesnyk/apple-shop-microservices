package org.productservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.productservice.dto.product.ProductResponse;
import org.productservice.entity.Product;
import org.productservice.exception.DuplicatedValueException;
import org.productservice.mapper.ProductMapper;
import org.productservice.repository.ProductRepository;
import org.productservice.service.FileService;
import org.productservice.service.KafkaService;
import org.productservice.service.ProductService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final ProductMapper productMapper;

    private final FileService s3Service;

    private final KafkaService kafkaService;

    @Override
    @Transactional("mongoTransactionManager")
    public ProductResponse createProduct(String skuCode, String name, String description, double price, List<String> categories, MultipartFile thumbnail, boolean isExclusive) throws ExecutionException, InterruptedException {
        log.info("Creating product with SKU: {}", skuCode);

        Product product = new Product();
        product.setSkuCode(skuCode);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setExclusive(isExclusive);

        String thumbnailUrl = s3Service.uploadFile(thumbnail);

        product.setCategories(categories);
        product.setThumbnailUrl(thumbnailUrl);

        if (productRepository.existsProductsBySkuCode(skuCode)) {
            log.error("Product with SKU Code {} already exists", skuCode);

            throw new DuplicatedValueException("SKU Code must be unique. This SKU Code already exists.");
        }

        Product result = productRepository.save(product);
        log.info("Product created successfully with ID: {}", result.getId());

        kafkaService.sendProductEventToKafka("CREATE", result);

        return productMapper.mapToProductResponse(result);
    }

    @Override
    public Page<ProductResponse> findAll(String categoryId, String search, Pageable pageable) {
        log.info("Fetching products with category: {}, search: {}, page: {} size: {}", categoryId, search, pageable.getPageNumber(), pageable.getPageSize());
        Page<Product> products = null;

        if (!search.isEmpty()) {
            products = productRepository.findAllByNameContainingIgnoreCase(search, pageable);
        } else if ("all".equals(categoryId)) {
            products = productRepository.findAll(pageable);
        } else {
            products = productRepository.findByCategoriesIn(List.of(categoryId), pageable);
        }

        log.info("Fetched {} products", products.getTotalElements());

        return products.map(product -> new ProductResponse(
                product.getId(),
                product.getSkuCode(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getThumbnailUrl(),
                product.getCategories(),
                product.isExclusive()
        ));
    }

    @Override
    @Transactional("mongoTransactionManager")
    @CacheEvict(value = "products", key = "#id")
    public void delete(String id) throws ExecutionException, InterruptedException {
        log.info("Attempting to delete product with ID: {}", id);

        Product product = productRepository.findById(id).orElseThrow(() -> {
            log.error("Product not found with ID: {}", id);
            return new RuntimeException("Product not found");
        });

        productRepository.delete(product);

        log.info("Product deleted successfully with ID: {}", id);

        s3Service.deleteFile(product.getThumbnailUrl());

        kafkaService.sendProductEventToKafka("DELETE", product);
    }

    public String uploadThumbnail(MultipartFile file, String id) {
        log.info("Uploading thumbnail for product ID: {}", id);

        Product product = productRepository.findById(id).orElseThrow(() -> {
            log.error("Product not found with ID: {}", id);
            return new RuntimeException("Product not found");
        });


        String thumbnailUrl = s3Service.uploadFile(file);

        product.setThumbnailUrl(thumbnailUrl);

        log.info("Thumbnail uploaded successfully for product ID: {}", id);
        productRepository.save(product);

        return thumbnailUrl;
    }

    @Cacheable(value = "products", key = "#id")
    public Product getProductById(String id) {
        log.info("Fetching product by ID: {}", id);

        return productRepository.findById(id).orElseThrow(() -> {
            log.error("Product not found with ID: {}", id);
            return new RuntimeException("Product not found");
        });
    }

    @Override
    @Transactional("mongoTransactionManager")
    @CachePut(value = "products", key = "#id")
    public ProductResponse updateProduct(String id, String name, String description, double price, MultipartFile thumbnail) throws ExecutionException, InterruptedException {
        log.info("Updating product with ID: {}", id);

        Product product = productRepository.findById(id).orElseThrow(() -> {
            log.error("Product not found with ID: {}", id);
            return new RuntimeException("Product not found");
        });

        product.setPrice(price);
        product.setName(name);
        product.setDescription(description);

        if (thumbnail != null) {
            log.info("Uploading new thumbnail for product ID: {}", id);
            String thumbnailUrl = s3Service.uploadFile(thumbnail);
            s3Service.deleteFile(product.getThumbnailUrl());
            product.setThumbnailUrl(thumbnailUrl);
        }

        Product result = productRepository.save(product);
        log.info("Product updated successfully with ID: {}", id);

        kafkaService.sendProductEventToKafka("UPDATE", result);

        return productMapper.mapToProductResponse(result);
    }

}
