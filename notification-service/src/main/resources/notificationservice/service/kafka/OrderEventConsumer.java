package org.service.notificationservice.service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.notificationservice.event.OrderEvent;
import org.service.notificationservice.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final NotificationService notificationService;

    @Transactional
    @KafkaListener(topics = "order-topic", containerFactory = "orderEventKafkaListenerContainerFactory")
    public void listen(@Payload OrderEvent orderEvent, Acknowledgment acknowledgment) {
        log.info("Received message from topic order-topic: {}", orderEvent);

        notificationService.handleOrderEvent(orderEvent);

        acknowledgment.acknowledge();
    }
}
