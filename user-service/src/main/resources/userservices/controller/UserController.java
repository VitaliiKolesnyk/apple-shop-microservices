package org.service.userservices.controller;

import org.service.userservices.dto.PasswordUpdateRequest;
import org.service.userservices.dto.User;
import org.service.userservices.dto.UserUpdateRequest;
import org.service.userservices.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public User getUserInfo(@RequestHeader(name = "Authorization") String bearerToken) {
        return userService.getUserInfo(bearerToken);
    }

    @PostMapping
    public void updateUserInfo(@RequestHeader(name = "Authorization") String bearerToken, @RequestBody UserUpdateRequest userUpdateRequest) {
        userService.updateUserInfo(userUpdateRequest, bearerToken);
    }

    @PutMapping("/updatePassword/{userId}")
    public void updatePassword(@RequestHeader(name = "Authorization") String bearerToken, @PathVariable String userId, @RequestBody PasswordUpdateRequest passwordUpdateRequest) {
        userService.updatePassword(passwordUpdateRequest, bearerToken, userId);
    }
}

