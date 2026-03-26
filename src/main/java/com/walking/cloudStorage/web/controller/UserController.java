package com.walking.cloudStorage.web.controller;

import com.walking.cloudStorage.config.security.UserPrincipal;
import com.walking.cloudStorage.service.UserService;
import com.walking.cloudStorage.web.dto.user.UserResponse;
import com.walking.cloudStorage.web.openapi.UserControllerApi;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController implements UserControllerApi {
    private final UserService userService;

    @Override
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return userService.getById(userPrincipal.getId());
    }
}
