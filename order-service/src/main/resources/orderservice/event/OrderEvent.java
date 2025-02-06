package org.service.orderservice.event;

public record OrderEvent(String status, String orderNumber, String name, String email) {
}
