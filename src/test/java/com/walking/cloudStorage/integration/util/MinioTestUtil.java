package com.walking.cloudStorage.integration.util;

import com.walking.cloudStorage.service.StorageService;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.experimental.UtilityClass;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

@UtilityClass
public class MinioTestUtil {
    public static void clearBucket(MinioClient minioClient, String bucket) throws Exception {
        for (Result<Item> item : minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .recursive(true)
                        .build())) {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(item.get().objectName())
                    .build());
        }
    }

    public static void uploadTestFile(StorageService storageService,
                                      String path,
                                      String fileName,
                                      String resourcePath,
                                      Long userId) throws Exception {
        storageService.uploadFile(path, userId, getMultipartFile(fileName, resourcePath));
    }

    public static void prepareDefaultData(StorageService storageService, Long userId) throws Exception {
        storageService.createDirectory("folder/docs/", userId);

        uploadTestFile(storageService, "folder/docs/", "file.txt", "data/file.txt", userId);
        uploadTestFile(storageService, "folder/", "file.txt", "data/file.txt", userId);
    }

    public static MockMultipartFile getMultipartFile(String filename, String resourcePath) throws Exception {
        return new MockMultipartFile(
                "object",
                filename,
                MediaType.MULTIPART_FORM_DATA_VALUE,
                new ClassPathResource(resourcePath).getInputStream()
        );
    }
}
