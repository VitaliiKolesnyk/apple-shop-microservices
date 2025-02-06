package org.service.inventoryservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.service.inventoryservice.service.CacheReservationService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CacheReservationServiceImpl implements CacheReservationService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String RESERVED_TICKET_PREFIX = "reserved:product:";

    public boolean isProductReserved(String skuCode) {
        String key = RESERVED_TICKET_PREFIX + skuCode;

        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public boolean reserveProduct(String skuCode) {
        String key = RESERVED_TICKET_PREFIX + skuCode;

        return redisTemplate.opsForValue().setIfAbsent(key, "lock", 15, TimeUnit.MINUTES);
    }

    public void releaseProduct(String skuCode) {
        String key = RESERVED_TICKET_PREFIX + skuCode;

        redisTemplate.delete(key);
    }
}
