package org.service.inventoryservice.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.inventoryservice.entity.Inventory;
import org.service.inventoryservice.repository.InventoryRepository;
import org.service.inventoryservice.service.InventoryService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendExceedLimitNotificationToKafka {

    private final InventoryRepository inventoryRepository;

    private final InventoryService inventoryService;

    private final RedisTemplate<String, Object> redisTemplate;

    @Scheduled(fixedRate = 600000)
    @Async
    public void checkInventoryAndNotify() {
        if (acquireLeadership()) {
            log.info("Checking inventory limits for all products");

            List<Inventory> inventories = inventoryRepository.findInventoriesExceedingLimit();

            inventories.forEach(inventory -> {
                try {
                    inventoryService.sendNotification(inventory);
                } catch (Exception ex) {
                    log.error("Error sending notification for SKU {}: {}", inventory.getProduct().getSkuCode(), ex.getMessage());
                }
            });

            log.info("Completed inventory limit check.");
        }
    }

    private boolean acquireLeadership() {
        String lockKey = "exceed-limit-leader-election-lock";
        Boolean isLeader = redisTemplate.opsForValue().setIfAbsent(lockKey, "lock", 540, TimeUnit.SECONDS);

        log.info("Leadership acquiring result - {}", isLeader);

        return Boolean.TRUE.equals(isLeader);
    }
}
