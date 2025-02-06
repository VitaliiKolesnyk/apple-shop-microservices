package org.productservice.mapper;


import org.productservice.dto.product.ProductResponse;
import org.productservice.entity.Product;


public interface ProductMapper {

    ProductResponse mapToProductResponse(Product product);
}
