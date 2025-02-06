package orderservice.mapper.impl;

import org.service.orderservice.dto.ProductDto;
import org.service.orderservice.entity.OrderProduct;
import org.service.orderservice.entity.Product;
import org.service.orderservice.mapper.ProductMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public Product map(ProductDto productDto) {
        Product product = new Product();
        product.setName(productDto.name());
        product.setPrice(productDto.price());
        product.setSkuCode(productDto.skuCode());

        return product;
    }

    @Override
    public ProductDto map(Product product, int quantity) {
        double totalAmount = quantity * product.getPrice();

        ProductDto productDto = new ProductDto(
                product.getName(), product.getDescription(), product.getPrice(), quantity, totalAmount, product.getThumbnailUrl());

        return productDto;
    }

    @Override
    public List<ProductDto> mapToDtoList(List<Product> products, List<OrderProduct> orderProducts) {
        List<ProductDto> result = new ArrayList<>();

        for (Product product : products) {
            int quantity = orderProducts.stream()
                    .filter(o -> o.getProduct().equals(product))
                    .findFirst()
                    .orElse(new OrderProduct(0))
                    .getQuantity();

            result.add(map(product, quantity));
        }

        return result;
    }

    @Override
    public List<Product> mapToEntityList(List<ProductDto> productDtos) {
        return productDtos.stream().map(product -> map(product)).toList();
    }
}
