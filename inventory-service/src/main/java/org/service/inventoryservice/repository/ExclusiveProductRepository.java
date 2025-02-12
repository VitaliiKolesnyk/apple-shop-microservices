package org.service.inventoryservice.repository;

import jakarta.persistence.LockModeType;
import org.service.inventoryservice.entity.ExclusiveProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExclusiveProductRepository extends JpaRepository<ExclusiveProduct, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ExclusiveProduct> findBySkuCode(String skuCode);
}
