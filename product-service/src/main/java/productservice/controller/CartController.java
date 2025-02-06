package productservice.controller;

import lombok.RequiredArgsConstructor;
import org.productservice.dto.cart.CartDto;
import org.productservice.entity.Product;
import org.productservice.service.CartService;
import org.productservice.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    private final ProductService productService;

    @PostMapping("/add/{userId}/{productId}/{quantity}")
    @ResponseStatus(HttpStatus.CREATED)
    public CartDto addProductToCart(@PathVariable String userId, @PathVariable String productId, @PathVariable int quantity) {
        Product product = productService.getProductById(productId);

        return cartService.addProductToCart(userId, product, quantity);
    }

    @GetMapping("/{userId}")
    public CartDto getCart(@PathVariable String userId) {
        return cartService.getCartByUserId(userId);
    }

    @DeleteMapping("/clear/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(@PathVariable String userId) {
        cartService.clearCart(userId);
    }

    @DeleteMapping("/{userId}/items/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public CartDto removeItemFromCart(@PathVariable String userId, @PathVariable String productId) {
        Product product = productService.getProductById(productId);

        return cartService.removeItemFromCart(userId, product);
    }

    @PutMapping("/{userId}/items/{productId}")
    public CartDto updateCartItemQuantity(
            @PathVariable String userId,
            @PathVariable String productId,
            @RequestParam int newQuantity) {

        return cartService.updateItemQuantity(userId, productId, newQuantity);
    }

    @GetMapping("/{userId}/totalPrice")
    public BigDecimal getCartTotalPrice(@PathVariable String userId) {
        return cartService.getCartTotalPrice(userId);
    }

    @GetMapping("/{userId}/totalQuantity")
    public Integer getCartQuantity(@PathVariable String userId) {
        return cartService.getCartTotalQuantity(userId);
    }
}
