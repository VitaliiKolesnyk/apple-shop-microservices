package orderservice.dto;

public record ProductDto(String name, String skuCode, Double price, int quantity, Double totalAmount, String thumbnailUrl) {
}
