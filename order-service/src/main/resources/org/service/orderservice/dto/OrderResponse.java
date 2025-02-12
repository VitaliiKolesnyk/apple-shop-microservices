package org.service.orderservice.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(Long id,
                            String orderNumber,
                            int quantity,
                            List<ProductDto> products,
                            ContactDetailsResponse contactDetails,
                            Double totalAmount,
                            Status status,
                            LocalDateTime orderedAt,
                            LocalDateTime deliveredAt) {
}
