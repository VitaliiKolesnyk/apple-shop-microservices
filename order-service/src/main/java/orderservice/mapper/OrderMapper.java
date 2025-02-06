package orderservice.mapper;

import org.service.orderservice.dto.OrderRequest;
import org.service.orderservice.dto.OrderResponse;
import org.service.orderservice.entity.Order;

import java.util.List;

public interface OrderMapper {

    OrderResponse map(Order order);

    Order map(OrderRequest orderRequest);

    List<OrderResponse> map(List<Order> orders);
}
