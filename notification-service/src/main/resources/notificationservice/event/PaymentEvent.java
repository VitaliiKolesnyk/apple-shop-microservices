package org.service.notificationservice.event;

public record PaymentEvent(String status, String orderNumber, String email) {
}