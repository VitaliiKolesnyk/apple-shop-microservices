package orderservice.service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.orderservice.event.OrderCancelEvent;
import org.service.orderservice.service.EventHandleService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCancelEventConsumer {

    private final EventHandleService eventHandleService;

    @KafkaListener(topics = {"order-cancel-events"}, containerFactory = "orderCancelEventListenerContainerFactory")
    public void listen(@Payload OrderCancelEvent orderCancelEvent, Acknowledgment acknowledgment) throws ExecutionException, InterruptedException {
        log.info("Received message from topic order-cancel-events: {}", orderCancelEvent);

        eventHandleService.handleOrderCancelEvent(orderCancelEvent);

        acknowledgment.acknowledge();
    }

}
