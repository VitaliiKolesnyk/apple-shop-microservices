package org.service.paymentservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.service.paymentservice.event.PaymentEvent;
import org.service.paymentservice.service.ProducerService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducer implements ProducerService {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @Override
    public void sendEventToKafka(PaymentEvent paymentEvent) throws ExecutionException, InterruptedException {
        log.info("Start - Sending payment event {} to Kafka topic payment-events", paymentEvent);

        ProducerRecord<String, PaymentEvent> record = new ProducerRecord<>(
                "payment-events", paymentEvent);

        log.info("Start - Sending productEvent {} to Kafka topic payment-events", paymentEvent);

        SendResult<String, PaymentEvent> result = kafkaTemplate.send(record).get();

        log.info("End - Sending productEvent to Kafka topic product-events. Partition - {}, offset - {}",
                result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
    }
}
