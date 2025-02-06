package org.service.userservices.dto;

public record UserUpdateRequest(String username, String firstName, String lastName, String email, Attribute attributes) {
}
