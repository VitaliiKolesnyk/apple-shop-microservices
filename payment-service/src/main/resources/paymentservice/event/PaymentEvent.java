package org.service.paymentservice.event;

import java.util.List;

public record PaymentEvent(String status, String orderNumber, String email, List<String> skuCodes) {
}
