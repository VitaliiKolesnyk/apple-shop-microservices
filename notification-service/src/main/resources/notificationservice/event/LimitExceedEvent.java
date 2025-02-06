package org.service.notificationservice.event;

import lombok.Data;

@Data
public class LimitExceedEvent {
    private String skuCode;
    private int limit;
    private String email;
}
