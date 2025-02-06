package notificationservice.service;

import org.service.notificationservice.event.LimitExceedEvent;
import org.service.notificationservice.event.OrderEvent;
import org.service.notificationservice.event.PaymentEvent;

public interface NotificationService {

    void handleOrderEvent(OrderEvent orderEvent);

    void handleLimitExceedEvent(LimitExceedEvent limitExceedEvent);

    void handlePaymentEvent(PaymentEvent paymentEvent);

}
