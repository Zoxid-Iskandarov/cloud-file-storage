package com.walking.cloudStorage.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walking.cloudStorage.integration.IntegrationTestBase;
import com.walking.cloudStorage.integration.annotation.WithMockUserPrincipal;
import com.walking.cloudStorage.web.dto.user.UserRequest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RequiredArgsConstructor
public class AuthControllerIT extends IntegrationTestBase {
    private final ObjectMapper objectMapper;

    @Test
    @Sql(scripts = "classpath:data/sql/cleanup.sql")
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
    @Sql(scripts = "classpath:data/sql/cleanup.sql")
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
    @Sql(scripts = {"classpath:data/sql/cleanup.sql", "classpath:data/sql/data.sql"})
    void signUp_whenUsernameAlreadyExists_failed() throws Exception {
        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getUserRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @Sql(scripts = {"classpath:data/sql/cleanup.sql", "classpath:data/sql/data.sql"})
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
    @Sql(scripts = "classpath:data/sql/cleanup.sql")
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
    @Sql(scripts = "classpath:data/sql/cleanup.sql")
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
    @Sql(scripts = {"classpath:data/sql/cleanup.sql", "classpath:data/sql/data.sql"})
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
    @Sql(scripts = {"classpath:data/sql/cleanup.sql", "classpath:data/sql/data.sql"})
    void signOut_whenUserAuthenticated_success() throws Exception {
        mockMvc.perform(post("/api/auth/sign-out"))
                .andExpect(status().isNoContent());
    }

    @Test
    @Sql(scripts = "classpath:data/sql/cleanup.sql")
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
