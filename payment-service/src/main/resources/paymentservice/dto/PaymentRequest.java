package org.service.paymentservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PaymentRequest {

    private BigDecimal amount;
    private String userId;
    private String userEmail;
    private String orderNumber;
    private List<String> skuCodes;

}

