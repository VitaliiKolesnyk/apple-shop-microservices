package productservice.dto.cart;

import org.productservice.entity.CartItem;

import java.util.List;

public record CartDto(String id, String userId, List<CartItem> cartItems, Double totalPrice) {
}
