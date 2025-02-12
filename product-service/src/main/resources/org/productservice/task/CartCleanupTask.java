package org.productservice.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.productservice.entity.Cart;
import org.productservice.repository.CartRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartCleanupTask {

    private final CartRepository cartRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    @Scheduled(fixedRate = 3600000)
    @Async
    public void cleanupExpiredCarts() {
        if (acquireLeadership()) {
            log.info("Starting cleanup of expired carts...");

            try {
                processExpiredCarts();
            } catch (Exception e) {
                log.error("Error cleaning up expired carts", e);
            }

            log.info("Expired carts cleanup task completed");
        }
    }

    private void processExpiredCarts() {
        LocalDateTime now = LocalDateTime.now();
        List<Cart> expiredCarts = cartRepository.findByExpirationDateBefore(now);

        expiredCarts.forEach(this::processExpiredCart);
    }

    @Transactional
    public void processExpiredCart(Cart cart) {
        String redisKey = "cart:" + cart.getUserId();
        redisTemplate.delete(redisKey);
        log.info("Cache cleared for cart with userId: {}", cart.getUserId());

        cartRepository.delete(cart);
        log.info("Cart with userId: {} deleted from MongoDB", cart.getUserId());
    }

    private boolean acquireLeadership() {
        String lockKey = "cart-cleanup-leader-election-lock";
        Boolean isLeader = redisTemplate.opsForValue().setIfAbsent(lockKey, new Object(), 3000, TimeUnit.SECONDS);

        log.info("Leadership acquiring result - {}", isLeader);

        return Boolean.TRUE.equals(isLeader);
    }
}
