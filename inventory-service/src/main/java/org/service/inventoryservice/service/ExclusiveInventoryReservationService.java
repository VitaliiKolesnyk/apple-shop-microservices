package org.service.inventoryservice.service;

public interface ExclusiveInventoryReservationService {

    boolean reserveProduct(String skuCode);

    boolean processProductAfterPayment(String skuCode);
}
