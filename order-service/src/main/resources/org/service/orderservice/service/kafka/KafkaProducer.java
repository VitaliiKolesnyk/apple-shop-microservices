package org.service.orderservice.service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.orderservice.event.OrderEvent;
import org.service.orderservice.service.ProducerService;
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
    public void sendEventToKafka(String status, String orderNumber, String name, String email) throws ExecutionException, InterruptedException {
        OrderEvent event = new OrderEvent(status, orderNumber, name, email);

        log.info("Start - Sending orderPlacedEvent {} to Kafka topic order-topic", event);

        SendResult<String, Object> result = kafkaTemplate.send("order-topic", event).get();

        log.info("End - Sending productEvent to Kafka topic order-topic. Partition - {}, offset - {}",
                result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
    }

}
