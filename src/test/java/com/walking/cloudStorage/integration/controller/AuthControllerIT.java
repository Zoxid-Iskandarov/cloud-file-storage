package com.walking.cloudStorage.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.testcontainers.RedisContainer;
import com.walking.cloudStorage.config.init.MinioInitializer;
import com.walking.cloudStorage.integration.annotation.WithMockUserPrincipal;
import com.walking.cloudStorage.service.impl.manager.DirectoryCreateManager;
import com.walking.cloudStorage.web.dto.user.UserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public class AuthControllerIT {
    private static final String POSTGRES_IMAGE_NAME = "postgres:17.2";
    private static final String REDIS_IMAGE_NAME = "redis:8.2";
    private static final Long USER_ID = 1L;
    private static final String USERNAME = "Zoxid27";
    private static final String VALID_PASSWORD = "Password123";
    private static final String INVALID_PASSWORD = "InvalidPassword123";

    @ServiceConnection
    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(POSTGRES_IMAGE_NAME);

    @Container
    private static final RedisContainer redisContainer = new RedisContainer(REDIS_IMAGE_NAME);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MinioInitializer minioInitializer;

    @MockitoBean
    private DirectoryCreateManager directoryCreateManager;

    @Test
    @Sql(scripts = "classpath:sql/cleanup.sql")
    void signUp_whenValidRequest_success() throws Exception {
        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getUserRequest())))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andExpect(jsonPath("$.created").exists());
    }

    @Test
    @Sql(scripts = "classpath:sql/cleanup.sql")
    void signUp_whenInvalidRequest_failed() throws Exception {
        UserRequest userRequest = getUserRequest();
        userRequest.setUsername("");
        userRequest.setPassword("");

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @Sql(scripts = {"classpath:sql/cleanup.sql", "classpath:sql/data.sql"})
    void signUp_whenUsernameAlreadyExists_failed() throws Exception {
        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getUserRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @Sql(scripts = {"classpath:sql/cleanup.sql", "classpath:sql/data.sql"})
    void signIn_whenValidRequest_success() throws Exception {
        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getUserRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andExpect(jsonPath("$.created").exists());
    }

    @Test
    @Sql(scripts = "classpath:sql/cleanup.sql")
    void signIn_whenInvalidRequest_failed() throws Exception {
        UserRequest userRequest = getUserRequest();
        userRequest.setUsername("");
        userRequest.setPassword("");

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @Sql(scripts = "classpath:sql/cleanup.sql")
    void signIn_whenUserNotFound_failed() throws Exception {
        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getUserRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    @Sql(scripts = {"classpath:sql/cleanup.sql", "classpath:sql/data.sql"})
    void signIn_whenPasswordIncorrect_failed() throws Exception {
        UserRequest userRequest = getUserRequest();
        userRequest.setPassword(INVALID_PASSWORD);

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    @WithMockUserPrincipal
    @Sql(scripts = {"classpath:sql/cleanup.sql", "classpath:sql/data.sql"})
    void signOut_whenUserAuthenticated_success() throws Exception {
        mockMvc.perform(post("/api/auth/sign-out"))
                .andExpect(status().isNoContent());
    }

    @Test
    @Sql(scripts = "classpath:sql/cleanup.sql")
    void signOut_whenUserNotAuthenticated_failed() throws Exception {
        mockMvc.perform(post("/api/auth/sign-out"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    private UserRequest getUserRequest() {
        return UserRequest.builder()
                .username(USERNAME)
                .password(VALID_PASSWORD)
                .build();
    }
}
