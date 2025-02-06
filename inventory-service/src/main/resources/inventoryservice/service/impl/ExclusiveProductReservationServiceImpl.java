package org.service.inventoryservice.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.inventoryservice.entity.ExclusiveProduct;
import org.service.inventoryservice.repository.ExclusiveProductRepository;
import org.service.inventoryservice.service.CacheReservationService;
import org.service.inventoryservice.service.ExclusiveInventoryReservationService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExclusiveProductReservationServiceImpl implements ExclusiveInventoryReservationService {

    private final CacheReservationService reservationService;

    private final ExclusiveProductRepository productRepository;

    @Transactional
    public boolean reserveProduct(String skuCode) {
        if (reservationService.isProductReserved(skuCode)) {
            log.warn("Product {} already reserved.", skuCode);
            return false;
        }

        Optional<ExclusiveProduct> productOpt = productRepository.findBySkuCode(skuCode);

        if (productOpt.isEmpty()) {
           log.warn("Exclusive Product {} Not Found", skuCode);
           return false;
        }

        ExclusiveProduct product = productOpt.get();

        if (!product.isAvailable()) {
            log.warn("Product {} is Not available.", skuCode);
            return false;
        }

        return reservationService.reserveProduct(product.getSkuCode());
    }

    @Transactional
    public boolean processProductAfterPayment(String skuCode) {
        log.info("Processing exclusive product with SKU {}", skuCode);

        Optional<ExclusiveProduct> productOpt = productRepository.findBySkuCode(skuCode);

        if (productOpt.isEmpty()) {
            log.warn("Exclusive Product {} Not Found", skuCode);
            return false;
        }

        ExclusiveProduct product = productOpt.get();

        completePurchase(product);

        log.info("Processed and released exclusive product with SKU {}", skuCode);

        return true;
    }

    private void completePurchase(ExclusiveProduct exclusiveProduct) {
        reservationService.releaseProduct(exclusiveProduct.getSkuCode());

        exclusiveProduct.setAvailable(false);

        productRepository.save(exclusiveProduct);
    }
}
