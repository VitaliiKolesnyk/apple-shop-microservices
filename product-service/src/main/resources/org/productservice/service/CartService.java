package org.productservice.service;

import org.productservice.dto.cart.CartDto;
import org.productservice.entity.Product;

import java.math.BigDecimal;

public interface CartService {

    CartDto addProductToCart(String userId, Product product, int quantity);

    CartDto getCartByUserId(String userId);

    void clearCart(String userId);

    CartDto removeItemFromCart(String userId, Product product);

    CartDto updateItemQuantity(String userId, String productId, int newQuantity);

    BigDecimal getCartTotalPrice(String userId);

    Integer getCartTotalQuantity(String userId);
}
