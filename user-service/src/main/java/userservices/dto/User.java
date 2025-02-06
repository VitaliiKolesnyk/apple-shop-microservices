package userservices.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class User{
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private Attribute attributes;
}
