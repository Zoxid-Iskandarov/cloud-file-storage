package com.walking.cloudStorage.integration.controller;

import com.walking.cloudStorage.integration.annotation.WithMockUserPrincipal;
import com.walking.cloudStorage.integration.util.MinioTestUtil;
import com.walking.cloudStorage.service.StorageService;
import io.minio.MinioClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@WithMockUserPrincipal
@Sql(scripts = {"classpath:data/sql/cleanup.sql", "classpath:data/sql/data.sql"})
public class DirectoryControllerIT {
    private static final String POSTGRES_IMAGE_NAME = "postgres:17.2";
    private static final String MINIO_IMAGE_NAME = "minio/minio:latest";
    private static final String BUCKET = "test-bucket";
    private static final Long USER_ID = 1L;

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(POSTGRES_IMAGE_NAME);

    @Container
    private static final MinIOContainer minIOContainer = new MinIOContainer(MINIO_IMAGE_NAME);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.endpoint", minIOContainer::getS3URL);
        registry.add("minio.access-key", minIOContainer::getUserName);
        registry.add("minio.secret-key", minIOContainer::getPassword);
        registry.add("minio.bucket", () -> BUCKET);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private StorageService storageService;

    @BeforeEach
    void setUp() throws Exception {
        MinioTestUtil.prepareDefaultData(storageService, USER_ID);
    }

    @AfterEach
    void tearDown() throws Exception {
        MinioTestUtil.clearBucket(minioClient, BUCKET);
    }

    @Test
    void getDirectoryContent_whenDirectoryExists_success() throws Exception {
        mockMvc.perform(get("/api/directory")
                        .param("path", "folder/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        [
                            {
                                "path": "folder/",
                                "name": "file.txt",
                                "size": 59,
                                "type": "FILE"
                            },
                            {
                                "path": "folder/",
                                "name": "docs",
                                "type": "DIRECTORY"
                            }
                        ]
                        """));
    }

    @Test
    void getDirectoryContent_whenDirectoryNotExists_failed() throws Exception {
        mockMvc.perform(get("/api/directory")
                .param("path", "new-folder/"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getDirectoryContent_whenInvalidParameter_failed() throws Exception {
        mockMvc.perform(get("/api/directory")
                        .param("path", "/folder"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void createDirectory_whenDirectoryNotExists_success() throws Exception {
        mockMvc.perform(post("/api/directory")
                        .param("path", "folder/images/"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.path").value("folder/"))
                .andExpect(jsonPath("$.name").value("images"))
                .andExpect(jsonPath("$.type").value("DIRECTORY"));
    }

    @Test
    void createDirectory_whenDirectoryAlreadyExists_failed() throws Exception {
        mockMvc.perform(post("/api/directory")
                        .param("path", "folder/docs/"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void createDirectory_whenInvalidParameter_failed() throws Exception {
        mockMvc.perform(post("/api/directory")
                        .param("path", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
