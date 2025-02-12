package org.productservice.mapper;

import org.productservice.dto.cart.CartDto;
import org.productservice.entity.Cart;

public interface CartMapper {

    CartDto toCartDto(Cart cart);
}
