package com.walking.cloudStorage.integration;

import com.redis.testcontainers.RedisContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTestBase {
    private static final String POSTGRES_IMAGE_NAME = "postgres:17.2";
    private static final String REDIS_IMAGE_NAME = "redis:8.2";
    private static final String MINIO_IMAGE_NAME = "minio/minio:latest";

    protected static final Long USER_ID = 1L;
    protected static final String USERNAME = "Zoxid27";
    protected static final String VALID_PASSWORD = "Password123";
    protected static final String INVALID_PASSWORD = "InvalidPassword123";
    protected static final String BUCKET = "test-bucket";

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(POSTGRES_IMAGE_NAME);

    @Container
    @ServiceConnection
    private static final RedisContainer redisContainer = new RedisContainer(REDIS_IMAGE_NAME);

    @Container
    private static final MinIOContainer minIOContainer = new MinIOContainer(MINIO_IMAGE_NAME);

    static {
        postgreSQLContainer.start();
        redisContainer.start();
        minIOContainer.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.endpoint", minIOContainer::getS3URL);
        registry.add("minio.access-key", minIOContainer::getUserName);
        registry.add("minio.secret-key", minIOContainer::getPassword);
        registry.add("minio.bucket", () -> BUCKET);
    }

    @Autowired
    protected MockMvc mockMvc;
}
