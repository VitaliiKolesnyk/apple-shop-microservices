package org.productservice.repository;

import org.productservice.entity.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CartRepository extends MongoRepository<Cart, String> {
    Optional<Cart> findByUserId(String userId);

    List<Cart> findByExpirationDateBefore(LocalDateTime now);
}
