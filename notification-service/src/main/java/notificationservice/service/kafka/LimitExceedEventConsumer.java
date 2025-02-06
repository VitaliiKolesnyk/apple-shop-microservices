package notificationservice.service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.notificationservice.event.LimitExceedEvent;
import org.service.notificationservice.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LimitExceedEventConsumer {

    private final NotificationService notificationService;

    @Transactional
    @KafkaListener(topics = "inventory-limit-topic", containerFactory = "limitExceedEventListenerContainerFactory")
    public void listen(@Payload LimitExceedEvent limitExceedEvent, Acknowledgment acknowledgment) {
        log.info("Received message from topic inventory-limit-topic: {}", limitExceedEvent);

        notificationService.handleLimitExceedEvent(limitExceedEvent);

        acknowledgment.acknowledge();
    }
}
