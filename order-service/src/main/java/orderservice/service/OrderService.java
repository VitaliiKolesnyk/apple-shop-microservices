package orderservice.service;


import org.service.orderservice.dto.OrderRequest;
import org.service.orderservice.dto.OrderResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface OrderService {

    OrderResponse placeOrder(OrderRequest orderRequest) throws ExecutionException, InterruptedException;

    List<OrderResponse> getOrders(String orderNumber);

    OrderResponse updateOrder(Long id, OrderRequest orderRequest) throws ExecutionException, InterruptedException;

    void deleteOrder(Long id);

    OrderResponse getOrder(Long id);

    List<OrderResponse> getOrdersByUserId(String userId);
}
