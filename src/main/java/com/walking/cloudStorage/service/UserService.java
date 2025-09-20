package com.walking.cloudStorage.service;

import com.walking.cloudStorage.config.security.UserPrincipal;
import com.walking.cloudStorage.web.dto.user.UserRequest;
import com.walking.cloudStorage.web.dto.user.UserResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    UserResponse getById(Long id);

    UserResponse create(UserRequest userRequest);

    UserPrincipal loadUserByUsername(String username);

    boolean existsByUsername(String username);
}
