package com.walking.cloudStorage.integration.controller;

import com.walking.cloudStorage.integration.IntegrationTestBase;
import com.walking.cloudStorage.integration.annotation.WithMockUserPrincipal;
import com.walking.cloudStorage.integration.util.MinioTestUtil;
import com.walking.cloudStorage.service.StorageService;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUserPrincipal
@Sql(scripts = {"classpath:data/sql/cleanup.sql", "classpath:data/sql/data.sql"})
@RequiredArgsConstructor
public class ResourceControllerIT extends IntegrationTestBase {
    private final MinioClient minioClient;
    private final StorageService storageService;

    @BeforeEach
    void setUp() throws Exception {
        MinioTestUtil.prepareDefaultData(storageService, USER_ID);
    }

    @AfterEach
    void tearDown() throws Exception {
        MinioTestUtil.clearBucket(minioClient, BUCKET);
    }

    @Test
    void getResourceInfo_whenResourceExists_success() throws Exception {
        mockMvc.perform(get("/api/resource")
                        .param("path", "folder/file.txt"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                            "path": "folder/",
                            "name": "file.txt",
                            "size": 59,
                            "type": "FILE"
                        }
                        """));
    }

    @Test
    void getResourceInfo_whenResourceNotExist_failed() throws Exception {
        mockMvc.perform(get("/api/resource")
                        .param("path", "folder/README.md"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getResourceInfo_whenInvalidParameter_failed() throws Exception {
        mockMvc.perform(get("/api/resource")
                        .param("path", "/folder"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void deleteResource_whenResourceExists_success() throws Exception {
        mockMvc.perform(delete("/api/resource")
                        .param("path", "folder/file.txt"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteResource_whenResourceNotExists_failed() throws Exception {
        mockMvc.perform(delete("/api/resource")
                        .param("path", "folder/README.md"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void deleteResource_whenInvalidParameter_failed() throws Exception {
        mockMvc.perform(delete("/api/resource")
                        .param("path", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void downloadResource_whenResourceExists_success() throws Exception {
        mockMvc.perform(get("/api/resource/download")
                        .param("path", "folder/file.txt"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
    }

    @Test
    void downloadResource_whenResourceNotExists_failed() throws Exception {
        mockMvc.perform(get("/api/resource/download")
                        .param("path", "folder/README.md"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void downloadResource_whenInvalidParameter_failed() throws Exception {
        mockMvc.perform(get("/api/resource/download")
                        .param("path", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void moveResource_whenResourceExistsRename_success() throws Exception {
        mockMvc.perform(get("/api/resource/move")
                        .param("from", "folder/file.txt")
                        .param("to", "folder/file2.txt"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                            "path": "folder/",
                            "name": "file2.txt",
                            "size": 59,
                            "type": "FILE"
                        }
                        """));
    }

    @Test
    void moveResource_whenResourceExistsMove_success() throws Exception {
        mockMvc.perform(get("/api/resource/move")
                        .param("from", "folder/file.txt")
                        .param("to", "documents/file.txt"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                            "path": "documents/",
                            "name": "file.txt",
                            "size": 59,
                            "type": "FILE"
                        }
                        """));
    }

    @Test
    void moveResource_whenResourceNotExists_failed() throws Exception {
        mockMvc.perform(get("/api/resource/move")
                        .param("from", "folder/file2.txt")
                        .param("to", "folder/docs/file2.txt"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void moveResource_whenResourceInTargetDirectoryAlreadyExists_failed() throws Exception {
        mockMvc.perform(get("/api/resource/move")
                        .param("from", "folder/file.txt")
                        .param("to", "folder/docs/file.txt"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void moveResource_whenInvalidParameter_failed() throws Exception {
        mockMvc.perform(get("/api/resource/move")
                        .param("from", "")
                        .param("to", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void searchResource_whenResourcesByQueryExists_success() throws Exception {
        mockMvc.perform(get("/api/resource/search")
                        .param("query", "file.txt"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        [
                            {
                                "path": "folder/docs/",
                                "name": "file.txt",
                                "size": 59,
                                "type": "FILE"
                            },
                            {
                                "path": "folder/",
                                "name": "file.txt",
                                "size": 59,
                                "type": "FILE"
                            }
                        ]
                        """));
    }

    @Test
    void searchResource_whenResourcesByQueryNotExists_success() throws Exception {
        mockMvc.perform(get("/api/resource/search")
                        .param("query", "not-exists"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
    }

    @Test
    void searchResource_whenInvalidParameter_failed() throws Exception {
        mockMvc.perform(get("/api/resource/search")
                        .param("query", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void uploadFile_success() throws Exception {
        mockMvc.perform(multipart("/api/resource")
                        .file(MinioTestUtil.getMultipartFile("file1", "data/file.txt"))
                        .file(MinioTestUtil.getMultipartFile("file2", "data/file.txt"))
                        .param("path", "new-folder/"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        [
                            {
                                "path": "new-folder/",
                                "name": "file1",
                                "size": 59,
                                "type": "FILE"
                            },
                            {
                                "path": "new-folder/",
                                "name": "file2",
                                "size": 59,
                                "type": "FILE"
                            }
                        ]
                        """));
    }

    @Test
    void uploadFile_whenResourceAlreadyExists_failed() throws Exception {
        mockMvc.perform(multipart("/api/resource")
                        .file(MinioTestUtil.getMultipartFile("file.txt", "data/file.txt"))
                        .param("path", "folder/"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void uploadFile_whenInvalidParameter_failed() throws Exception {
        mockMvc.perform(multipart("/api/resource")
                        .file(MinioTestUtil.getMultipartFile("", "data/file.txt"))
                        .param("path", "folder/"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
