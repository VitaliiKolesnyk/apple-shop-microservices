package org.service.orderservice.dto;

public record ContactDetailsResponse(String name, String surname, String email, String phone,
                                     String country, String city, String street) {
}
