package inventoryservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.inventoryservice.dto.InventoryRequest;
import org.service.inventoryservice.dto.InventoryResponse;
import org.service.inventoryservice.entity.Inventory;
import org.service.inventoryservice.event.*;
import org.service.inventoryservice.mapper.InventoryMapper;
import org.service.inventoryservice.repository.InventoryRepository;
import org.service.inventoryservice.service.InventoryService;
import org.service.inventoryservice.service.ProducerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    private final InventoryMapper inventoryMapper;

    private final ProducerService producerService;

    @Override
    public InventoryResponse update(Long id, InventoryRequest inventoryRequest) {
        log.info("Updating inventory with ID: {}", id);

        Inventory inventory = inventoryRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Inventory not found"));

        log.info("Inventory found for ID: {}", id);

        inventory.setLimit(inventoryRequest.limit());
        inventory.setQuantity(inventoryRequest.quantity());
        inventory.setLimitNotificationSent(false);

        Inventory savedInventory = inventoryRepository.save(inventory);

        log.info("Inventory updated successfully for ID: {}", id);

        return inventoryMapper.mapToInventoryResponse(savedInventory);
    }

    @Override
    public List<InventoryResponse> findAll() {
        return inventoryMapper.mapToInventoryResponseList(inventoryRepository.findAll());
    }

    @Override
    public InventoryResponse findBySkuCode(String skuCode) {
        return inventoryMapper.mapToInventoryResponse(inventoryRepository.findByProductSkuCode(skuCode)
                .orElseThrow(() -> new RuntimeException("Inventory not found")));
    }

    @Transactional("transactionManager")
    public void sendNotification(Inventory inventory) {
        try {
            log.warn("Inventory limit exceeded for SKU: {}, Quantity: {}",
                    inventory.getProduct().getSkuCode(), inventory.getQuantity());

            LimitExceedEvent limitExceedEvent = new LimitExceedEvent();
            limitExceedEvent.setSkuCode(inventory.getProduct().getSkuCode());
            limitExceedEvent.setLimit(inventory.getQuantity());

            producerService.sendLimitExceedEventToKafka(limitExceedEvent);

            updateIsNotificationSentFlag(inventory);
        } catch (Exception e) {
            log.error("Error processing inventory notification for SKU {}: {}", inventory.getProduct().getSkuCode(), e.getMessage(), e);
        }
    }

    public void updateIsNotificationSentFlag(Inventory inventory) {
        inventory.setLimitNotificationSent(true);
        inventoryRepository.save(inventory);
    }

}
