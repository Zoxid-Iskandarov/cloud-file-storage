package com.walking.cloudStorage.service.impl.manager;

import com.walking.cloudStorage.util.MinioUtil;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.walking.cloudStorage.util.PathUtil.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceDeleteManager {
    private final MinioUtil minioUtil;

    public void deleteResource(String path, Long userId) {
        if (isDirectory(path)) {
            deleteDirectory(path, userId);
        } else {
            deleteFile(path, userId);
        }
    }

    private void deleteDirectory(String path, Long userId) {
        String objectName = buildObjectName(path, userId);

        minioUtil.directoryExistsOrThrow(objectName, userId);

        try {
            Iterable<Result<Item>> results = minioUtil.listObjects(objectName, true);

            for (Result<Item> result : results) {
                minioUtil.removeObject(result.get().objectName());
            }
        } catch (Exception e) {
            log.error("Failed to delete directory '{}', userId={}", objectName, userId, e);
            throw new RuntimeException("Failed to delete directory: " + path, e);
        }
    }

    private void deleteFile(String path, Long userId) {
        String objectName = buildObjectName(path, userId);

        minioUtil.fileExistsOrThrow(objectName, userId);

        try {
            minioUtil.removeObject(objectName);
        } catch (Exception e) {
            log.error("Failed to delete file '{}', userId={}", objectName, userId, e);
            throw new RuntimeException("Failed to delete file: " + path, e);
        }
    }
}
