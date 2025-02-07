package org.service.inventoryservice.repository;

import jakarta.transaction.Transactional;
import org.service.inventoryservice.entity.ProductReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductReservationRepository extends JpaRepository<ProductReservation, Long> {

    @Modifying
    @Transactional
    void deleteAllByOrderNumber(String orderNumber);

    List<ProductReservation> findAllByReservationUntilDateLessThan(LocalDateTime localDateTime);

    List<ProductReservation> findAllByOrderNumber(String orderNumber);
}
