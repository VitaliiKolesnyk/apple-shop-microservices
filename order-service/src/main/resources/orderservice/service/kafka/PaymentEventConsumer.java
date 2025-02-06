package org.service.orderservice.service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.orderservice.event.PaymentEvent;
import org.service.orderservice.service.EventHandleService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final EventHandleService eventHandleService;

    @KafkaListener(topics = "payment-events", containerFactory = "paymentEventListenerContainerFactory")
    public void listen(@Payload PaymentEvent paymentEvent, Acknowledgment acknowledgment) {
        log.info("Received message from topic payment-events: {}", paymentEvent);

        eventHandleService.handlePaymentEvent(paymentEvent);

        acknowledgment.acknowledge();
    }

}
