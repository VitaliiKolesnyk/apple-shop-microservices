package notificationservice.service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.notificationservice.event.PaymentEvent;
import org.service.notificationservice.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final NotificationService notificationService;

    @Transactional
    @KafkaListener(topics = "payment-events", containerFactory = "paymentEventKafkaListenerContainerFactory")
    public void listen(@Payload PaymentEvent paymentEvent, Acknowledgment acknowledgment) {
        log.info("Received message from topic payment-events: {}",paymentEvent);

        notificationService.handlePaymentEvent(paymentEvent);

        acknowledgment.acknowledge();
    }
}
