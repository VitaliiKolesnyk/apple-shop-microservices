package org.service.orderservice.mapper.impl;

import lombok.RequiredArgsConstructor;
import org.service.orderservice.dto.OrderRequest;
import org.service.orderservice.dto.OrderResponse;
import org.service.orderservice.entity.Order;
import org.service.orderservice.mapper.ContactDetailsMapper;
import org.service.orderservice.mapper.OrderMapper;
import org.service.orderservice.mapper.ProductMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderMapperImpl implements OrderMapper {

    private final ProductMapper productMapper;

    private final ContactDetailsMapper contactDetailsMapper;

    public OrderResponse map(Order order) {
        return new OrderResponse(order.getId(), order.getOrderNumber(), order.getQuantity(),
                productMapper.mapToDtoList(order.getProducts(), order.getOrderProducts()), contactDetailsMapper.map(order.getContactDetails()),
                order.getTotalAmount(), order.getStatus(), order.getOrderedAt(), order.getDeliveredAt());
    }

    public Order map(OrderRequest orderRequest) {
        return Order.builder()
                .id(orderRequest.id())
                .userId(orderRequest.userId())
                .orderNumber(orderRequest.orderNumber())
                .quantity(orderRequest.quantity())
                .status(orderRequest.status())
                .contactDetails(contactDetailsMapper.map(orderRequest.contactDetails()))
                .orderProducts(new ArrayList<>())
                .build();
    }

    @Override
    public List<OrderResponse> map(List<Order> orders) {
        return orders.stream().
                map(order -> map(order))
                .toList();
    }
}
