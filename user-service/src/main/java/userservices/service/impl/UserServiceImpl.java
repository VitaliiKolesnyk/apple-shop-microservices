package userservices.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.userservices.dto.PasswordUpdateRequest;
import org.service.userservices.dto.User;
import org.service.userservices.dto.UserUpdateRequest;
import org.service.userservices.service.UserService;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String KEYCLOAK_USER_URL = "http://keycloak.default.svc.cluster.local:8080/realms/dev/account";

    private final RestTemplate restTemplate;

    @Override
    public User getUserInfo(String token) {
        log.info("Start - Retrieving user info for token: {}", token);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<User> response = null;
        try {
            response = restTemplate.exchange(KEYCLOAK_USER_URL, HttpMethod.GET, entity, User.class);
            log.info("Successfully retrieved user info for token: {}", token);
        } catch (Exception e) {
            log.error("Error retrieving user info for token: {}. Error: {}", token, e.getMessage());
            throw new RuntimeException("Error retrieving user info from Keycloak");
        }

        return response.getBody();
    }

    @Override
    public void updateUserInfo(UserUpdateRequest userUpdateRequest, String token) {
        log.info("Start - Updating user info for token: {}", token);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        HttpEntity<UserUpdateRequest> entity = new HttpEntity<>(userUpdateRequest, headers);

        try {
            restTemplate.postForObject(KEYCLOAK_USER_URL, entity, String.class);
            log.info("Successfully updated user info for token: {}", token);
        } catch (Exception e) {
            log.error("Error updating user info for token: {}. Error: {}", token, e.getMessage());
            throw new RuntimeException("Error updating user info in Keycloak");
        }
    }

    public void updatePassword(PasswordUpdateRequest passwordUpdateRequest, String token, String userId) {
        String url = "http://keycloak.default.svc.cluster.local:8080/admin/realms/dev/users/" + userId + "/reset-password";

        log.info("Start - Updating password for userId: {}", userId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        headers.set("Content-Type", "application/json");

        passwordUpdateRequest.setType("password");
        passwordUpdateRequest.setTemporary(false);

        HttpEntity<PasswordUpdateRequest> requestEntity = new HttpEntity<>(passwordUpdateRequest, headers);

        try {
            restTemplate.put(url, requestEntity);
            log.info("Successfully updated password for userId: {}", userId);
        } catch (Exception e) {
            log.error("Error updating password for userId: {}. Error: {}", userId, e.getMessage());
            throw new RuntimeException("Error updating password in Keycloak");
        }
    }
}
