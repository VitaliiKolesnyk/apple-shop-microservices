package orderservice.event;

public record PaymentEvent(String status, String orderNumber, String email) {
}
