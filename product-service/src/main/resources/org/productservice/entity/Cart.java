package org.productservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(value = "cart")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Cart {

    private String id;

    private String userId;

    private List<CartItem> cartItems;

    private Double totalPrice;

    private LocalDateTime expirationDate;

    public Cart(String userId,List<CartItem> cartItems) {
        this.userId = userId;
        this.cartItems = cartItems;
    }

    public void calculateTotalPrice() {
        totalPrice = cartItems.stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();
    }

}
