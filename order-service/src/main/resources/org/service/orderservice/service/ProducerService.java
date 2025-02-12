package org.service.orderservice.service;

import java.util.concurrent.ExecutionException;

public interface ProducerService {

    void sendEventToKafka(String status, String orderNumber, String name, String email) throws ExecutionException, InterruptedException;
}
