package inventoryservice.mapper;

import org.service.inventoryservice.dto.ProductDto;
import org.service.inventoryservice.entity.ProductReservation;


public interface ProductMapper {

    ProductReservation map(ProductDto productDto, String orderNumber);
}
