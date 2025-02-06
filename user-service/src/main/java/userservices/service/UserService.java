package userservices.service;

import org.service.userservices.dto.PasswordUpdateRequest;
import org.service.userservices.dto.User;
import org.service.userservices.dto.UserUpdateRequest;

public interface UserService {

    User getUserInfo(String token);

    void updateUserInfo(UserUpdateRequest userUpdateRequest, String token);

    void updatePassword(PasswordUpdateRequest passwordUpdateRequest, String token, String userId);

}
