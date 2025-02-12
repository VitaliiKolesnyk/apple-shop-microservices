package org.productservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.productservice.dto.cart.CartDto;
import org.productservice.entity.Cart;
import org.productservice.entity.CartItem;
import org.productservice.entity.Product;
import org.productservice.mapper.CartMapper;
import org.productservice.repository.CartRepository;
import org.productservice.service.CartService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    private final CartMapper cartMapper;

    private static final Integer EXPIRATION_HOURS_QUANTITY = 12;

    @CachePut(value = "cart", key = "#userId")
    public CartDto addProductToCart(String userId, Product product, int quantity) {
        log.info("Adding product with ID {} to cart for user {}", product.getId(), userId);

        Optional<Cart> optionalCart = cartRepository.findByUserId(userId);
        Cart cart = optionalCart.orElse(new Cart(userId, new ArrayList<>()));

        Optional<CartItem> existingItem = cart.getCartItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.calculateTotalPrice();
            log.info("Updated quantity of product with ID {} in the cart. New quantity: {}", product.getId(), item.getQuantity());
        } else {
            CartItem newItem = new CartItem(product, quantity, product.getPrice() * quantity, product.getThumbnailUrl());
            cart.getCartItems().add(newItem);
            log.info("Added new product with ID {} to the cart", product.getId());
        }

        cart.calculateTotalPrice();

        updateExpirationDate(cart);

        return cartMapper.toCartDto(cartRepository.save(cart));
    }

    @Cacheable(value = "cart", key = "#userId", unless = "#result == null")
    public CartDto getCartByUserId(String userId) {
        log.info("Fetching cart for user {}", userId);

        return cartMapper.toCartDto(cartRepository.findByUserId(userId).orElse(new Cart(userId, new ArrayList<>())));
    }

    @CacheEvict(value = "cart", key = "#userId")
    public void clearCart(String userId) {
        log.info("Clearing cart for user {}", userId);

        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Cart not found"));
        cart.getCartItems().clear();
        cart.setTotalPrice(0.0);
        cartRepository.save(cart);

        redisTemplate.delete("cart:" + userId);
    }

    @CachePut(value = "cart", key = "#userId")
    @Override
    public CartDto removeItemFromCart(String userId, Product product) {
        log.info("Removing product with ID {} from cart for user {}", product.getId(), userId);

        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> {
            log.error("Cart not found for user {}", userId);
            return new RuntimeException("Cart not found");
        });

        List<CartItem> cartItems = cart.getCartItems();

        cartItems.removeIf(cartItem -> cartItem.getProduct().equals(product));

        cart.calculateTotalPrice();

        Cart updatedCart = cartRepository.save(cart);

        log.info("Product with ID {} removed from cart for user {}", product.getId(), userId);

        updateExpirationDate(cart);

        return cartMapper.toCartDto(updatedCart);
    }

    @CachePut(value = "cart", key = "#userId")
    @Override
    public CartDto updateItemQuantity(String userId, String productId, int newQuantity) {
        log.info("Updating quantity for product with ID {} in cart for user {} to {}", productId, userId, newQuantity);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("Cart not found for user {}", userId);
                    return new RuntimeException("Cart not found for userId: " + userId);
                });

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Product with ID {} not found in cart for user {}", productId, userId);
                    return new RuntimeException("Product with ID " + productId + " not found in cart");
                });

        cartItem.setQuantity(newQuantity);
        cartItem.calculateTotalPrice();

        cart.calculateTotalPrice();

        updateExpirationDate(cart);

        log.info("Product with ID {} in cart for user {} updated to quantity {}", productId, userId, newQuantity);
        log.info("Updated cart for user {} with new total price: {}", userId, cart.getTotalPrice());

        return cartMapper.toCartDto(cartRepository.save(cart));
    }

    @Override
    public BigDecimal getCartTotalPrice(String userId) {
        log.info("Getting total price for cart of user {}", userId);

        Optional<Cart> cart = cartRepository.findByUserId(userId);

        return cart.map(value -> BigDecimal.valueOf(value.getTotalPrice())).orElse(BigDecimal.ZERO);

    }

    @Override
    public Integer getCartTotalQuantity(String userId) {
        log.info("Getting total quantity for cart of user {}", userId);

        Optional<Cart> cart = cartRepository.findByUserId(userId);

        return cart.map(value -> value
                .getCartItems()
                .stream().mapToInt(CartItem::getQuantity)
                .sum()).orElse(0);

    }

    private void updateExpirationDate(Cart cart) {
        cart.setExpirationDate(LocalDateTime.now().plusHours(EXPIRATION_HOURS_QUANTITY));
    }
}
