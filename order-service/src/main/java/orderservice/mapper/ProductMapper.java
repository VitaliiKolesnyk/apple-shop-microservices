package orderservice.mapper;

import org.service.orderservice.dto.ProductDto;
import org.service.orderservice.entity.OrderProduct;
import org.service.orderservice.entity.Product;

import java.util.List;

public interface ProductMapper {

    Product map(ProductDto productDto);

    ProductDto map(Product product, int quantity);

    List<ProductDto> mapToDtoList(List<Product> products, List<OrderProduct> orderProducts);

    List<Product> mapToEntityList(List<ProductDto> productDtos);
}
