package org.service.orderservice.repository;

import org.service.orderservice.dto.Status;
import org.service.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findOrdersByUserId(String userId);

    Optional<Order> findOrderByOrderNumber(String orderNumber);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.orderedAt < :orderedAt AND o.isNotPaidReminderSent = :notPaidReminderSent")
    List<Order> findAllByStatusAndOrderedAtLessThanAndNotPaidReminderSent(
            Status status, LocalDateTime orderedAt, boolean notPaidReminderSent);
}
