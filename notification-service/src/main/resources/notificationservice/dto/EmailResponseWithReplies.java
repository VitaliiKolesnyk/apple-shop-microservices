package org.service.notificationservice.dto;

import java.time.LocalDateTime;
import java.util.List;

public record EmailResponseWithReplies(Long id, String subject, String body, String email, Status status, LocalDateTime timestamp, List<ReplyResponse> replies) {
}
