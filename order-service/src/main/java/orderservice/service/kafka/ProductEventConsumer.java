package orderservice.service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.orderservice.event.ProductEvent;
import org.service.orderservice.service.EventHandleService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEventConsumer {

    private final EventHandleService eventHandleService;

    @KafkaListener(topics = "product-events", containerFactory = "productEventListenerContainerFactory")
    public void listen(@Payload ProductEvent productEvent, Acknowledgment acknowledgment) {
        log.info("Received message from topic product-events: {}", productEvent);

        eventHandleService.handleProductEvent(productEvent);

        acknowledgment.acknowledge();
    }

}
