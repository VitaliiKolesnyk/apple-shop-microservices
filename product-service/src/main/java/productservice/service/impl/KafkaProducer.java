package productservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.productservice.entity.Product;
import org.productservice.event.ProductEvent;
import org.productservice.service.KafkaService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducer implements KafkaService {

    private final KafkaTemplate<String, ProductEvent> kafkaTemplate;

    public void sendProductEventToKafka(String action, Product product) throws ExecutionException, InterruptedException {
        ProductEvent productEvent = new ProductEvent(action, product.getName(), product.getSkuCode(), product.getDescription(),
                product.getPrice(), product.getThumbnailUrl(), product.isExclusive());

        ProducerRecord<String, ProductEvent> record = new ProducerRecord<>(
                "product-events", productEvent);

        log.info("Start - Sending productEvent {} to Kafka topic product-events", productEvent);

        SendResult<String, ProductEvent> result = kafkaTemplate.send(record).get();

        log.info("End - Sending productEvent to Kafka topic product-events. Partition - {}, offset - {}",
                result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
    }

}
