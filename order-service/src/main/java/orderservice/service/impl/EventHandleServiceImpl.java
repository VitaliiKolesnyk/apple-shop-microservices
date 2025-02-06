package orderservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.orderservice.dto.Status;
import org.service.orderservice.entity.ContactDetails;
import org.service.orderservice.entity.Order;
import org.service.orderservice.entity.Product;
import org.service.orderservice.event.OrderCancelEvent;
import org.service.orderservice.event.PaymentEvent;
import org.service.orderservice.event.ProductEvent;
import org.service.orderservice.repository.OrderRepository;
import org.service.orderservice.repository.ProductRepository;
import org.service.orderservice.service.EventHandleService;
import org.service.orderservice.service.OrderService;
import org.service.orderservice.service.kafka.KafkaProducer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventHandleServiceImpl implements EventHandleService {

    private final ProductRepository productRepository;

    private final OrderRepository orderRepository;

    private final KafkaProducer kafkaProducer;

    private final OrderService orderService;

    @Override
    public void handleProductEvent(ProductEvent productEvent) {
        log.info("Got Message from product-events topic {}", productEvent);

        String action = productEvent.action();

        switch (action) {
            case "CREATE": saveProduct(productEvent);
                break;
            case "DELETE": deleteProduct(productEvent);
                break;
            case "UPDATE": updateProduct(productEvent);
                break;
        }
    }

    private void saveProduct(ProductEvent productEvent) {
        Product product = new Product();
        product.setName(productEvent.name());
        product.setDescription(productEvent.description());
        product.setPrice(productEvent.price());
        product.setSkuCode(productEvent.skuCode());
        product.setThumbnailUrl(productEvent.thumbnailUrl());

        productRepository.save(product);

        log.info("Product with sku code {} was saved successfully", productEvent.skuCode());
    }

    private void deleteProduct(ProductEvent productEvent) {
        Product product = productRepository.findBySkuCode(productEvent.skuCode())
                .orElseThrow(() -> new RuntimeException("Product not found"));


        productRepository.delete(product);

        log.info("Product with sku code {} was deleted successfully", productEvent.skuCode());
    }

    private void updateProduct(ProductEvent productEvent) {
        Product product = productRepository.findBySkuCode(productEvent.skuCode())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setName(productEvent.name());
        product.setDescription(productEvent.description());
        product.setPrice(productEvent.price());
        product.setThumbnailUrl(productEvent.thumbnailUrl());

        productRepository.save(product);

        log.info("Product with sku code {} was updated successfully", productEvent.skuCode());
    }

    @Override
    @Transactional("transactionManager")
    public void handleOrderCancelEvent(OrderCancelEvent orderCancelEvent) throws ExecutionException, InterruptedException {
        log.info("Got Message from order-cancel-events topic {}", orderCancelEvent);

        Order order = orderRepository.findOrderByOrderNumber(orderCancelEvent.orderNumber())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getStatus().equals(Status.CANCELLED)) {
            order.setStatus(Status.CANCELLED);

            orderRepository.save(order);

            String orderNumber = order.getOrderNumber();

            log.info("Order {} successfully updated to CANCELLED status", orderNumber);

            ContactDetails contactDetails = getContactDetailsByOrderNumber(orderNumber);

            kafkaProducer.sendEventToKafka("Cancelled", orderNumber, contactDetails.getName(), contactDetails.getEmail());

            log.info("Order {} cancelled, notifying via email: {}", orderNumber, contactDetails.getEmail());
        }
    }

    @Override
    public void handlePaymentEvent(PaymentEvent paymentEvent) {
        log.info("Got Message from payment-events topic {}", paymentEvent);

        if ("Success".equals(paymentEvent.status())) {
            Order order = orderRepository.findOrderByOrderNumber(paymentEvent.orderNumber())
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            order.setStatus(Status.PAID);

            orderRepository.save(order);

            log.info("Order {} successfully updated to PAID status", order.getOrderNumber());
        }
    }

     private ContactDetails getContactDetailsByOrderNumber(String orderNumber) {
        Order order = orderRepository.findOrderByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return order.getContactDetails();
    }
}
