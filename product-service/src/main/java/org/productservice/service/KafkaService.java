package org.productservice.service;

import org.productservice.entity.Product;

import java.util.concurrent.ExecutionException;

public interface KafkaService {

    void sendProductEventToKafka(String action, Product product) throws ExecutionException, InterruptedException;
}
