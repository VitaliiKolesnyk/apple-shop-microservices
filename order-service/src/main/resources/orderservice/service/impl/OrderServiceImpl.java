package org.service.orderservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.orderservice.client.InventoryClient;
import org.service.orderservice.dto.*;
import org.service.orderservice.entity.ContactDetails;
import org.service.orderservice.entity.Order;
import org.service.orderservice.entity.OrderProduct;
import org.service.orderservice.entity.Product;
import org.service.orderservice.exception.NotInStockException;
import org.service.orderservice.mapper.OrderMapper;
import org.service.orderservice.repository.OrderRepository;
import org.service.orderservice.repository.ProductRepository;
import org.service.orderservice.service.OrderService;
import org.service.orderservice.service.ProducerService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final ProductRepository productRepository;

    private final OrderMapper orderMapper;

    private final InventoryClient inventoryClient;

    private final ProducerService kafkaService;

    @Override
    @Transactional("transactionManager")
    public OrderResponse placeOrder(OrderRequest orderRequest) throws ExecutionException, InterruptedException {
        String orderNumber = generateOrderNumber();
        ReserveProductsRequest reserveProductsRequest = new ReserveProductsRequest(orderRequest.products(), orderNumber);

        try {
            log.info("Reserving products for order: {}", orderNumber);
            reserveProducts(reserveProductsRequest);
        } catch (RuntimeException e) {
            log.error("Failed to reserve products: {}", e.getMessage());
            throw e;
        }

        Double totalAmount = orderRequest.products().stream().mapToDouble(dto -> dto.price()).sum() * orderRequest.quantity();
        log.debug("Total amount for order {}: {}", orderNumber, totalAmount);

        Order order = orderMapper.map(orderRequest);
        order.setTotalAmount(totalAmount);
        order.setOrderNumber(orderNumber);
        order.setQuantity(orderRequest.quantity());
        order.setStatus(Status.NEW);
        order.setProducts(getProducts(orderRequest.products(), order));
        order.setOrderedAt(LocalDateTime.now());
        setOrderProduct(order, orderRequest.products());
        ContactDetails contactDetails = order.getContactDetails();
        contactDetails.setOrder(order);

        Order savedOrder = orderRepository.save(order);
        log.info("Order placed successfully with number: {}", savedOrder.getOrderNumber());

        kafkaService.sendEventToKafka("Placed", orderNumber, contactDetails.getName(), contactDetails.getEmail());

        return orderMapper.map(savedOrder);
    }

    private static String generateOrderNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String shortTimestamp = timestamp.substring(timestamp.length() - 6);

        Random random = new Random();
        int randomNumber = 10 + random.nextInt(90);

        return shortTimestamp + "-" + randomNumber;
    }

    public void reserveProducts(ReserveProductsRequest reserveProductsRequest) {
        log.info("Start - Reserving products for order: {}", reserveProductsRequest.orderNumber());
        ResponseEntity<Boolean> responseEntity = inventoryClient.reserveProducts(reserveProductsRequest);

        if (responseEntity.getStatusCode().is5xxServerError() && responseEntity.getBody() != null && !responseEntity.getBody()) {
            log.error("Failed to reserve products due to server error for order: {}", reserveProductsRequest.orderNumber());
            throw new RuntimeException("Circuit breaker enabled");
        }

        if (responseEntity.getBody() != null && !responseEntity.getBody()) {
            log.error("Products are out of stock for order: {}", reserveProductsRequest.orderNumber());
            throw new NotInStockException("Products are out of stock");
        }

        log.info("Successfully reserved products for order: {}", reserveProductsRequest.orderNumber());
    }

    @Override
    public List<OrderResponse> getOrders(String orderNumber) {
        List<Order> orders;

        if (orderNumber == null || orderNumber.isEmpty()) {
            orders = orderRepository.findAll();
        } else {
            orders = List.of(orderRepository.findOrderByOrderNumber(orderNumber).
                    orElseThrow(() -> new RuntimeException("Order not found")));
        }

        return orderMapper.map(orders);
    }

    @Override
    public OrderResponse updateOrder(Long id, OrderRequest orderRequest) throws ExecutionException, InterruptedException {
        log.info("Start - Updating order with id: {} and status: {}", id, orderRequest.status());

        Order order = orderRepository.findById(id).orElseThrow( () ->
                new RuntimeException("Order with id " + orderRequest.id() + " not found"));

        order.setStatus(orderRequest.status());
        log.debug("Order {} status updated to: {}", orderRequest.orderNumber(), orderRequest.status());

        if (orderRequest.status().equals(Status.DELIVERED)) {
            order.setDeliveredAt(LocalDateTime.now());
            log.info("Order {} marked as delivered", id);
        }

        if (orderRequest.status().equals(Status.CANCELLED)) {
            String orderNumber = orderRequest.orderNumber();
            ContactDetails contactDetails = getContactDetailsByOrderNumber(orderNumber);
            log.info("Order {} cancelled, notifying via email: {}", orderNumber, contactDetails.getEmail());
            kafkaService.sendEventToKafka("Cancelled", orderNumber, contactDetails.getName(), contactDetails.getEmail());
        }

        log.info("Order with id {} updated successfully", id);

       return orderMapper.map(orderRepository.save(order));
    }

    @Override
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);

        log.info("Order with id {} deleted successfully", id);
    }

    @Override
    public OrderResponse getOrder(Long id) {
        Order order =  orderRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Order not found"));

        return orderMapper.map(order);
    }

    @Override
    public List<OrderResponse> getOrdersByUserId(String userId) {
        return orderMapper.map(orderRepository.findOrdersByUserId(userId));
    }

    private List<Product> getProducts(List<ProductDto> productDtos, Order order) {
        List<Product> products = new ArrayList<>();

        productDtos.forEach(productDto -> {
            Optional<Product> product = productRepository.findBySkuCode(productDto.skuCode());

            if (product.isPresent()) {
                Product productEntity = product.get();
                products.add(productEntity);

                productEntity.getOrders().add(order);
            }
        });

        return products;
    }

    private void setOrderProduct(Order order, List<ProductDto> products) {
        for (ProductDto product : products) {
            Product productFromDb = productRepository.findBySkuCode(product.skuCode())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setProduct(productFromDb);
            orderProduct.setOrder(order);
            orderProduct.setQuantity(product.quantity());

            order.getOrderProducts().add(orderProduct);
            productFromDb.getOrderProducts().add(orderProduct);
        }
    }

    private ContactDetails getContactDetailsByOrderNumber(String orderNumber) {
        Order order = orderRepository.findOrderByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return order.getContactDetails();
    }



}
