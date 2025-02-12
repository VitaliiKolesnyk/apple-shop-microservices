package org.productservice.mapper.impl;

import org.productservice.dto.cart.CartDto;
import org.productservice.entity.Cart;
import org.productservice.mapper.CartMapper;
import org.springframework.stereotype.Component;

@Component
public class CartMapperImpl implements CartMapper {
    @Override
    public CartDto toCartDto(Cart cart) {
        return new CartDto(cart.getId(), cart.getUserId(), cart.getCartItems(), cart.getTotalPrice());
    }
}
