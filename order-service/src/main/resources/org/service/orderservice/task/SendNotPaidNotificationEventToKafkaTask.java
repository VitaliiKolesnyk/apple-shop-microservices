package org.service.orderservice.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.orderservice.dto.Status;
import org.service.orderservice.entity.ContactDetails;
import org.service.orderservice.entity.Order;
import org.service.orderservice.repository.OrderRepository;
import org.service.orderservice.service.ProducerService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class SendNotPaidNotificationEventToKafkaTask {

    private final ProducerService kafkaService;

    private final OrderRepository orderRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    private static final int NOT_PAID_REMINDER_MINUTES_AFTER_ORDER_CREATION = 7;

    @Scheduled(fixedRate = 120000)
    @Async
    public void sendPaymentNotification() {
        if (acquireLeadership()) {
            log.info("Checking for unpaid orders");

            // Fetch all not-paid orders older than 7 minutes
            List<Order> notPaidOrders = orderRepository.findAllByStatusAndOrderedAtLessThanAndNotPaidReminderSent(
                    Status.NEW, LocalDateTime.now().minusMinutes(NOT_PAID_REMINDER_MINUTES_AFTER_ORDER_CREATION), false);

            if (notPaidOrders.isEmpty()) {
                log.info("No unpaid orders found.");
                return;
            }

            notPaidOrders.forEach(this::handleNotPaidEvent);

            log.info("All unpaid orders have been submitted for processing.");
        }
    }

    @Transactional("transactionManager")
    public void handleNotPaidEvent(Order notPaidOrder) {
        notPaidOrder.setNotPaidReminderSent(true);

        orderRepository.save(notPaidOrder);

        log.info("Unpaid order with id {} have been updated", notPaidOrder.getId());

        sendNotPaidEventToKafka(notPaidOrder);
    }

    private void sendNotPaidEventToKafka(Order notPaidOrder) {
        try {
            ContactDetails contactDetails = notPaidOrder.getContactDetails();
            kafkaService.sendEventToKafka(
                    "Not Paid",
                    notPaidOrder.getOrderNumber(),
                    contactDetails.getName(),
                    contactDetails.getEmail()
            );
            log.info("Sent 'Not Paid' event for order {}", notPaidOrder.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to send 'Not Paid' event for order {}: {}",
                    notPaidOrder.getOrderNumber(), e.getMessage(), e);
        }
    }

    private boolean acquireLeadership() {
        String lockKey = "not_paid-notification-leader-election-lock";
        Boolean isLeader = redisTemplate.opsForValue().setIfAbsent(lockKey, new Object(), 110, TimeUnit.SECONDS);

        log.info("Leadership acquiring result - {}", isLeader);

        return Boolean.TRUE.equals(isLeader);
    }
}
