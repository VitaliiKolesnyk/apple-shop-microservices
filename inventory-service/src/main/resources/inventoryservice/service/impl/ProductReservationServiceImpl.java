package org.service.inventoryservice.service.impl;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.inventoryservice.dto.ProductDto;
import org.service.inventoryservice.dto.ReserveRequest;
import org.service.inventoryservice.entity.*;
import org.service.inventoryservice.event.OrderCancelEvent;
import org.service.inventoryservice.exception.NotInStockException;
import org.service.inventoryservice.mapper.ProductMapper;
import org.service.inventoryservice.repository.ExclusiveProductRepository;
import org.service.inventoryservice.repository.InventoryRepository;
import org.service.inventoryservice.repository.ProductRepository;
import org.service.inventoryservice.repository.ProductReservationRepository;
import org.service.inventoryservice.service.ExclusiveInventoryReservationService;
import org.service.inventoryservice.service.ProducerService;
import org.service.inventoryservice.service.ProductReservationService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductReservationServiceImpl implements ProductReservationService {

    private final ProductReservationRepository productReservationRepository;

    private final ProductRepository productRepository;

    private final ExclusiveProductRepository exclusiveProductRepository;

    private final InventoryRepository inventoryRepository;

    private final ProductMapper productMapper;

    private final ProducerService producerService;

    private final ExclusiveInventoryReservationService exclusiveInventoryReservationService;

    @Transactional
    @Override
    public boolean reserveInventory(ReserveRequest reserveRequest) {
        log.info("Reserving inventory for order {}", reserveRequest.orderNumber());

        for (ProductDto productDto : reserveRequest.products()) {
            log.info("Processing product with SKU: {}", productDto.skuCode());

            Optional<Product> productOpt = productRepository.findBySkuCode(productDto.skuCode());
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                log.info("Product {} found", productDto.skuCode());

                boolean success = reserveProductInventory(productDto, reserveRequest.orderNumber(), product);
                if (!success) {
                    return false;
                }
            } else {
                boolean success = processExclusiveProductWithLock(productDto.skuCode());
                if (!success) {
                    return false;
                }
            }
        }

        log.info("Inventory successfully reserved");
        return true;
    }

    @Transactional
    public boolean processExclusiveProductWithLock(String skuCode) {
        log.info("Locking and processing exclusive product with SKU: {}", skuCode);

        Optional<ExclusiveProduct> exclusiveProductOpt = exclusiveProductRepository.findBySkuCode(skuCode);

        if (exclusiveProductOpt.isEmpty()) {
            log.warn("Exclusive Product {} not found", skuCode);
            return false;
        }

        ExclusiveProduct exclusiveProduct = exclusiveProductOpt.get();

        boolean success = exclusiveInventoryReservationService.reserveProduct(exclusiveProduct.getSkuCode());

        if (!success) {
            log.warn("Reservation failed for exclusive product with SKU: {}", skuCode);
            return false;
        }

        log.info("Exclusive product {} successfully reserved", skuCode);

        return true;
    }

    @Transactional
    public boolean reserveProductInventory(ProductDto productDto, String orderNumber, Product product) {
        int maxRetries = 3;
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                Inventory inventory = inventoryRepository.findByProductSkuCode(productDto.skuCode())
                        .orElseThrow(() -> new NotInStockException("Inventory not found"));

                log.info("Inventory found for SKU: {}", productDto.skuCode());

                if (inventory.getQuantity() < productDto.quantity()) {
                    log.warn("Not enough stock for SKU: {}", productDto.skuCode());
                    return false;
                }

                inventory.setQuantity(inventory.getQuantity() - productDto.quantity());
                inventoryRepository.save(inventory);

                ProductReservation productReservation = productMapper.map(productDto, orderNumber);
                productReservation.setProduct(product);
                product.getProductReservations().add(productReservation);
                productReservation.setReservationUntilDate(LocalDateTime.now().plusMinutes(10));
                productReservationRepository.save(productReservation);

                log.info("Reserved {} units of SKU: {}", productDto.quantity(), productDto.skuCode());
                return true;
            } catch (OptimisticLockException e) {
                attempt++;
                log.warn("Optimistic lock failed for SKU: {}. Retrying {}/{}", productDto.skuCode(), attempt, maxRetries);
                if (attempt >= maxRetries) {
                    log.error("Failed to reserve inventory for SKU: {} after {} attempts", productDto.skuCode(), maxRetries);
                    return false;
                }
            }
        }

        return false;
    }

    private void addQuantityToInventory(String skuCode, Integer quantity) {
        final int MAX_RETRIES = 3;
        int attempt = 0;

        log.info("Adding {} units to inventory for SKU: {}", quantity, skuCode);

        while (attempt < MAX_RETRIES) {
            try {
                Inventory inventory = inventoryRepository.findByProductSkuCode(skuCode).orElseThrow(
                        () -> new RuntimeException("Inventory not found"));

                log.info("Inventory found for SKU: {}", skuCode);

                inventory.setQuantity(inventory.getQuantity() + quantity);

                inventoryRepository.save(inventory);

                log.info("Inventory updated successfully for SKU: {}", skuCode);

                break;

            } catch (OptimisticLockingFailureException e) {
                attempt++;

                log.warn("Optimistic locking failure on attempt {} for SKU: {}", attempt, skuCode);

                if (attempt >= MAX_RETRIES) {
                    log.error("Failed to update inventory due to concurrent modifications for SKU: {}", skuCode, e);

                    throw new RuntimeException("Failed to update inventory due to concurrent modifications for SKU: " + skuCode, e);
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Transactional("transactionManager")
    public void cancelReservation(ProductReservation productReservation) throws ExecutionException, InterruptedException {
        productReservationRepository.delete(productReservation);
        log.info("Canceled reservation for order {}", productReservation.getOrderNumber());

        restoreInventory(productReservation);

        sendCancellationEvent(productReservation);
    }

    private void restoreInventory(ProductReservation productReservation) {
        addQuantityToInventory(productReservation.getProduct().getSkuCode(), productReservation.getQuantity());
        log.info("Restored {} units to inventory for SKU: {}",
                productReservation.getQuantity(),
                productReservation.getProduct().getSkuCode());
    }

    private void sendCancellationEvent(ProductReservation productReservation) throws ExecutionException, InterruptedException {
        String orderNumber = productReservation.getOrderNumber();
        producerService.sendOrderCancelEventToKafka(new OrderCancelEvent(orderNumber));
        log.info("Sent order cancel event for order {}", orderNumber);
    }

}
