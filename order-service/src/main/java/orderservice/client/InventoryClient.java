package orderservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.service.orderservice.dto.ReserveProductsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;


public interface InventoryClient {

    Logger log = LoggerFactory.getLogger(InventoryClient.class);

    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @PostExchange("/api/inventory/reserveProducts")
    ResponseEntity<Boolean> reserveProducts(@RequestBody ReserveProductsRequest reserveProductsRequest);

    default ResponseEntity<Boolean> fallbackMethod(ReserveProductsRequest reserveProductsRequest, Throwable throwable) {
        log.warn("Cannot get inventory, failure reason: {}", throwable.getMessage());

        return new ResponseEntity<>(Boolean.FALSE, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
