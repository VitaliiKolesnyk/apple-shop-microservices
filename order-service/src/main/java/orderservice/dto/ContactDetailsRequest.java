package orderservice.dto;

public record ContactDetailsRequest(String name, String surname, String email, String phone,
                                    String country, String city, String street) {
}
