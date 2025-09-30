package com.walking.cloudStorage.integration.service;

import com.walking.cloudStorage.config.init.MinioInitializer;
import com.walking.cloudStorage.domain.exception.AuthenticationException;
import com.walking.cloudStorage.service.AuthService;
import com.walking.cloudStorage.service.impl.manager.DirectoryCreateManager;
import com.walking.cloudStorage.web.dto.user.UserRequest;
import com.walking.cloudStorage.web.dto.user.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
public class AuthServiceIT {
    private static final String POSTGRES_IMAGE_NAME = "postgres:17.2";
    private static final Long USER_ID = 1L;
    private static final String USERNAME = "Zoxid27";
    private static final String VALID_PASSWORD = "Password123";
    private static final String INVALID_PASSWORD = "InvalidPassword123";

    @ServiceConnection
    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(POSTGRES_IMAGE_NAME);

    @Autowired
    private AuthService authService;

    @Autowired
    private HttpServletResponse response;

    @Autowired
    private HttpServletRequest request;

    @MockitoBean
    private MinioInitializer minioInitializer;

    @MockitoBean
    private DirectoryCreateManager directoryCreateManager;

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

        verify(directoryCreateManager).createDirectory("", USER_ID);
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
