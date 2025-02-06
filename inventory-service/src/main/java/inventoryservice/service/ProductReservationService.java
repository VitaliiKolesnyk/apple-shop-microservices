package inventoryservice.service;

import org.service.inventoryservice.dto.ReserveRequest;
import org.service.inventoryservice.entity.ProductReservation;

import java.util.concurrent.ExecutionException;

public interface ProductReservationService {

    boolean reserveInventory(ReserveRequest reserveRequest);

    void cancelReservation(ProductReservation productReservation) throws ExecutionException, InterruptedException;
}
