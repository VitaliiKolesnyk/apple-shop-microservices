package org.service.notificationservice.dto;

import java.time.LocalDateTime;

public record EmailResponse(Long id, String subject, String email, Status status, LocalDateTime timestamp, LastReplyFrom lastReplyFrom, LocalDateTime lastReplyAt) {

}
