package inventoryservice.service;

public interface CacheReservationService {

    boolean isProductReserved(String skuCode);

    boolean reserveProduct(String skuCode);

    void releaseProduct(String skuCode);
}
