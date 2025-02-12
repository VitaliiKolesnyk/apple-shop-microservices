package org.service.inventoryservice.service;

import org.service.inventoryservice.event.PaymentEvent;
import org.service.inventoryservice.event.ProductEvent;

public interface EventHandleService {

    void handleProductEvent(ProductEvent productEvent);

    void handlePaymentEvent(PaymentEvent paymentEvent);

}
