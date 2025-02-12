package org.service.orderservice.dto;

import java.util.List;

public record OrderRequest(Long id,
                           String orderNumber,
                           String userId,
                           Integer quantity,
                           List<ProductDto> products,
                           UserDetails userDetails,
                           ContactDetailsRequest contactDetails,
                           Status status) {
}
