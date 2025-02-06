package org.service.notificationservice.dto;

public record ContactRequest(String email, String subject, String emailBody, String userId, boolean isAdmin) {
}
