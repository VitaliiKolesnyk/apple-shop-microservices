package org.service.orderservice.service;

import org.service.orderservice.event.OrderCancelEvent;
import org.service.orderservice.event.PaymentEvent;
import org.service.orderservice.event.ProductEvent;

import java.util.concurrent.ExecutionException;

public interface EventHandleService {

    void handleProductEvent(ProductEvent productEvent);

    void handlePaymentEvent(PaymentEvent paymentEvent);

    void handleOrderCancelEvent(OrderCancelEvent orderCancelEvent) throws ExecutionException, InterruptedException;
}
