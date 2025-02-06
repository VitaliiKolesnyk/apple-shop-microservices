package paymentservice.service;

import org.service.paymentservice.event.PaymentEvent;

import java.util.concurrent.ExecutionException;

public interface ProducerService {

    void sendEventToKafka(PaymentEvent paymentEvent) throws ExecutionException, InterruptedException;

}
