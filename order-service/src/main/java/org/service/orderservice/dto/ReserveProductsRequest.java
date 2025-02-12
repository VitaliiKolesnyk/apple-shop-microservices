package org.service.orderservice.dto;

import java.util.List;

public record ReserveProductsRequest(List<ProductDto> products, String orderNumber) {
}
