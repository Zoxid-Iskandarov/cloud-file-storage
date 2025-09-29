package com.walking.cloudStorage.integration.controller;

import com.redis.testcontainers.RedisContainer;
import com.walking.cloudStorage.config.init.MinioInitializer;
import com.walking.cloudStorage.integration.annotation.WithMockUserPrincipal;
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
public class UserControllerIT {
    private static final String POSTGRES_IMAGE_NAME = "postgres:17.2";
    private static final String REDIS_IMAGE_NAME = "redis:8.2";
    private static final Long USER_ID = 1L;
    private static final String USERNAME = "Zoxid27";

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

    @MockitoBean
    private MinioInitializer minioInitializer;

    @Test
    @WithMockUserPrincipal
    @Sql(scripts = {"classpath:sql/cleanup.sql", "classpath:sql/data.sql"})
    void me_whenUserAuthenticated_success() throws Exception {
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andExpect(jsonPath("$.created").exists());
    }

    @Test
    @Sql(scripts = "classpath:sql/cleanup.sql")
    void me_whenUserNotAuthenticated_failed() throws Exception {
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication required"))
                .andExpect(jsonPath("$.path").value("/api/user/me"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
