package org.service.inventoryservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.inventoryservice.entity.ExclusiveProduct;
import org.service.inventoryservice.entity.Inventory;
import org.service.inventoryservice.entity.Product;
import org.service.inventoryservice.event.PaymentEvent;
import org.service.inventoryservice.event.ProductEvent;
import org.service.inventoryservice.repository.ExclusiveProductRepository;
import org.service.inventoryservice.repository.InventoryRepository;
import org.service.inventoryservice.repository.ProductRepository;
import org.service.inventoryservice.repository.ProductReservationRepository;
import org.service.inventoryservice.service.EventHandleService;
import org.service.inventoryservice.service.ExclusiveInventoryReservationService;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class EventHandleServiceImpl implements EventHandleService {

    private final InventoryRepository inventoryRepository;

    private final ProductReservationRepository productReservationRepository;

    private final ProductRepository productRepository;

    private final ExclusiveProductRepository exclusiveProductRepository;

    private final ExclusiveInventoryReservationService exclusiveInventoryReservationService;

    @Override
    public void handleProductEvent(ProductEvent productEvent) {
        log.info("Got Message from product-events topic {}", productEvent);

        String action = productEvent.action();

        switch (action) {
            case "CREATE": saveInventory(productEvent);
                break;
            case "DELETE": deleteInventory(productEvent);
                break;
            case "UPDATE": updateInventory(productEvent);
                break;
            default:
                log.warn("Unknown product event action: {}", action);
        }
    }

    private void saveInventory(ProductEvent productEvent) {
        log.info("Saving new inventory for product {}", productEvent.skuCode());

        if (!productEvent.isExclusive()) {
            Product product = new Product();
            product.setName(productEvent.name());
            product.setSkuCode(productEvent.skuCode());
            product.setThumbnailUrl(productEvent.thumbnailUrl());

            Inventory inventory = new Inventory();
            inventory.setProduct(product);
            inventory.setQuantity(0);
            inventory.setLimit(0);
            product.setInventory(inventory);

            inventoryRepository.save(inventory);

            log.info("Inventory saved successfully for product {}", productEvent.skuCode());
        } else {
            ExclusiveProduct exclusiveProduct = new ExclusiveProduct();
            exclusiveProduct.setSkuCode(productEvent.skuCode());
            exclusiveProduct.setAvailable(true);

            exclusiveProductRepository.save(exclusiveProduct);

            log.info("Inventory saved successfully for exclusive product {}", productEvent.skuCode());
        }
    }


    private void deleteInventory(ProductEvent productEvent) {
        log.info("Deleting inventory for product {}", productEvent.skuCode());

        inventoryRepository.delete(inventoryRepository.findByProductSkuCode(productEvent.skuCode())
                .orElseThrow(() -> new RuntimeException("Inventory not found")));
    }

    public void updateInventory(ProductEvent productEvent) {
        log.info("Updating inventory for product {}", productEvent.skuCode());

        Product product = productRepository.findBySkuCode(productEvent.skuCode())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setName(productEvent.name());
        product.setThumbnailUrl(productEvent.thumbnailUrl());

        productRepository.save(product);

        log.info("Inventory deleted for product {}", productEvent.skuCode());
    }

    @Override
    public void handlePaymentEvent(PaymentEvent paymentEvent) {
        log.info("Got Message from payment-events topic {}", paymentEvent);

        if (paymentEvent.status().equals("Success")) {
            for (String skuCode : paymentEvent.skuCodes()) {
                exclusiveInventoryReservationService.processProductAfterPayment(skuCode);
            }

            productReservationRepository.deleteAllByOrderNumber(paymentEvent.orderNumber());
            log.info("Deleted reservations for order {}", paymentEvent.orderNumber());
        }
    }
}
