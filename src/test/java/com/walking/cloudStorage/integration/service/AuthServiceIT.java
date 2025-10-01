package com.walking.cloudStorage.integration.service;

import com.walking.cloudStorage.domain.exception.AuthenticationException;
import com.walking.cloudStorage.integration.IntegrationTestBase;
import com.walking.cloudStorage.service.AuthService;
import com.walking.cloudStorage.web.dto.user.UserRequest;
import com.walking.cloudStorage.web.dto.user.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@RequiredArgsConstructor
public class AuthServiceIT extends IntegrationTestBase {
    private final AuthService authService;
    private final HttpServletResponse response;
    private final HttpServletRequest request;

    @Test
    @Sql(scripts = "classpath:data/sql/cleanup.sql")
    void signUp_whenValidRequest_success() {
        UserRequest userRequest = getUserRequest();

        UserResponse userResponse = authService.signUp(userRequest, request, response);

        assertThatAuthenticationIsSuccessful();

        assertThat(userResponse).isNotNull();
        assertThat(userResponse.getId()).isEqualTo(USER_ID);
        assertThat(userResponse.getUsername()).isEqualTo(userRequest.getUsername());
        assertThat(userResponse.getCreated()).isNotNull();
    }

    @Test
    @Sql(scripts = {"classpath:data/sql/cleanup.sql", "classpath:data/sql/data.sql"})
    void signIn_whenValidRequest_success() {
        UserRequest userRequest = getUserRequest();

        UserResponse userResponse = authService.signIn(userRequest, request, response);

        assertThatAuthenticationIsSuccessful();

        assertThat(userResponse).isNotNull();
        assertThat(userResponse.getId()).isEqualTo(USER_ID);
        assertThat(userResponse.getUsername()).isEqualTo(userRequest.getUsername());
        assertThat(userResponse.getCreated()).isNotNull();
    }

    @Test
    @Sql(scripts = "classpath:data/sql/cleanup.sql")
    void signIn_whenUsernameNotExists_failed() {
        assertThrows(AuthenticationException.class, () -> authService.signIn(getUserRequest(), request, response));
    }

    @Test
    @Sql(scripts = {"classpath:data/sql/cleanup.sql", "classpath:data/sql/data.sql"})
    void signIn_whenPasswordIncorrect_failed() {
        UserRequest userRequest = getUserRequest();
        userRequest.setPassword(INVALID_PASSWORD);

        assertThrows(AuthenticationException.class, () -> authService.signIn(userRequest, request, response));
    }

    private void assertThatAuthenticationIsSuccessful() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getName()).isEqualTo(USERNAME);
    }

    private UserRequest getUserRequest() {
        return UserRequest.builder()
                .username(USERNAME)
                .password(VALID_PASSWORD)
                .build();
    }
}
