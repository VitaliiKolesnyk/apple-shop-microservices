package inventoryservice.service;

import org.service.inventoryservice.event.LimitExceedEvent;
import org.service.inventoryservice.event.OrderCancelEvent;

import java.util.concurrent.ExecutionException;

public interface ProducerService {

    void sendLimitExceedEventToKafka(LimitExceedEvent limitExceedEvent) throws ExecutionException, InterruptedException;

    void sendOrderCancelEventToKafka(OrderCancelEvent orderCancelEvent) throws ExecutionException, InterruptedException;
}
