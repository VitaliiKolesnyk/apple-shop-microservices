package org.service.inventoryservice.service.kafka;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.inventoryservice.event.LimitExceedEvent;
import org.service.inventoryservice.event.OrderCancelEvent;
import org.service.inventoryservice.service.ProducerService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducer implements ProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void sendLimitExceedEventToKafka(LimitExceedEvent limitExceedEvent) throws ExecutionException, InterruptedException {
        log.info("Start - Sending limitExceedEvent {} to Kafka topic inventory-limit-topic", limitExceedEvent);

        SendResult<String, Object> result = kafkaTemplate.send("product-events", limitExceedEvent).get();

        log.info("End - Sending productEvent to Kafka topic inventory-limit-topic. Partition - {}, offset - {}",
                result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
    }

    @Override
    public void sendOrderCancelEventToKafka(OrderCancelEvent orderCancelEvent) throws ExecutionException, InterruptedException {
        log.info("Start - Sending orderCancelEvent {} to Kafka topic order-cancel-events", orderCancelEvent);

        SendResult<String, Object> result = kafkaTemplate.send("order-cancel-events", orderCancelEvent).get();

        log.info("End - Sending productEvent to Kafka topic order-cancel-events. Partition - {}, offset - {}",
                result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
    }
}
