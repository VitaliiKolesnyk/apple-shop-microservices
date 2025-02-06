package org.productservice.mapper.impl;

import org.productservice.dto.product.ProductResponse;
import org.productservice.entity.Product;
import org.productservice.mapper.ProductMapper;
import org.springframework.stereotype.Component;

@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public ProductResponse mapToProductResponse(Product product) {
        return new ProductResponse(product.getId(), product.getSkuCode(),
                product.getName(), product.getDescription(), product.getPrice(), product.getThumbnailUrl(),
                product.getCategories(), product.isExclusive());
    }
}
