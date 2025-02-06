package org.service.inventoryservice.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.inventoryservice.entity.ProductReservation;
import org.service.inventoryservice.repository.ProductReservationRepository;
import org.service.inventoryservice.service.ProductReservationService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class CancelReservationTask {

    private final ProductReservationRepository productReservationRepository;

    private final ProductReservationService productReservationService;

    private final RedisTemplate<String, Object> redisTemplate;

    @Scheduled(fixedRate = 900000)
    @Async
    public void cancelReservation() {
        if (acquireLeadership()) {
            log.info("Checking for expired product reservations");

            List<ProductReservation> expiredReservations =
                    productReservationRepository.findAllByReservationUntilDateLessThan(LocalDateTime.now());

            if (expiredReservations.isEmpty()) {
                log.info("No expired reservations found.");
                return;
            }

            expiredReservations.forEach(reservation -> {
                try {
                    productReservationService.cancelReservation(reservation);
                } catch (Exception e) {
                    log.error("Error processing reservation for order {}: {}", reservation.getOrderNumber(), e.getMessage(), e);
                }
            });

            log.info("Completed processing of expired reservations.");
        }
    }

    private boolean acquireLeadership() {
        String lockKey = "cancel-reservation-leader-election-lock";
        Boolean isLeader = redisTemplate.opsForValue().setIfAbsent(lockKey, "lock", 840, TimeUnit.SECONDS);

        log.info("Leadership acquiring result - {}", isLeader);

        return Boolean.TRUE.equals(isLeader);
    }
}
