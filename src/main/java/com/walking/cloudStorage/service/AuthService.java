package com.walking.cloudStorage.service;

import com.walking.cloudStorage.web.dto.user.UserRequest;
import com.walking.cloudStorage.web.dto.user.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    UserResponse signUp(UserRequest userRequest, HttpServletRequest request, HttpServletResponse response);

    UserResponse signIn(UserRequest userRequest, HttpServletRequest request, HttpServletResponse response);

    void signOut(HttpServletRequest request);
}
