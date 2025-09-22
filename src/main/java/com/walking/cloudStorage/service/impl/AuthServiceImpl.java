package com.walking.cloudStorage.service.impl;

import com.walking.cloudStorage.config.security.UserPrincipal;
import com.walking.cloudStorage.domain.exception.AuthenticationException;
import com.walking.cloudStorage.service.AuthService;
import com.walking.cloudStorage.service.UserService;
import com.walking.cloudStorage.web.dto.user.UserRequest;
import com.walking.cloudStorage.web.dto.user.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserService userService;
    private final SecurityContextRepository securityContextRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse signUp(UserRequest userRequest, HttpServletRequest request, HttpServletResponse response) {
        UserResponse user = userService.create(userRequest);

        UserPrincipal userPrincipal = userService.loadUserByUsername(userRequest.getUsername());
        authenticate(userPrincipal, request, response);

        return user;
    }

    @Override
    public UserResponse signIn(UserRequest userRequest, HttpServletRequest request, HttpServletResponse response) {
        if (!userService.existsByUsername(userRequest.getUsername())) {
            throw new AuthenticationException("Invalid username or password");
        }

        UserPrincipal userPrincipal = userService.loadUserByUsername(userRequest.getUsername());

        if (!passwordEncoder.matches(userRequest.getPassword(), userPrincipal.getPassword())) {
            throw new AuthenticationException("Invalid username or password");
        }

        authenticate(userPrincipal, request, response);

        return userService.getById(userPrincipal.getId());
    }

    @Override
    public void signOut(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        SecurityContextHolder.clearContext();
    }

    private void authenticate(UserPrincipal userPrincipal, HttpServletRequest request, HttpServletResponse response) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, request, response);
    }
}
