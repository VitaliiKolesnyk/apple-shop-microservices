package userservices.dto;

import lombok.Data;

@Data
public class PasswordUpdateRequest {
    private String type;
    private String value;
    private boolean temporary;
}
