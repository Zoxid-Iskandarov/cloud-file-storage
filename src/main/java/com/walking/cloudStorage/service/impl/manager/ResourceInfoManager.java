package com.walking.cloudStorage.service.impl.manager;

import com.walking.cloudStorage.util.MinioUtil;
import com.walking.cloudStorage.web.dto.resource.ResourceResponse;
import io.minio.StatObjectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.walking.cloudStorage.util.PathUtil.*;
import static com.walking.cloudStorage.util.PathUtil.isDirectory;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceInfoManager {
    private final MinioUtil minioUtil;

    public ResourceResponse getResourceInfo(String path, Long userId) {
        return isDirectory(path) ? getDirectoryInfo(path, userId) : getFileInfo(path, userId);
    }

    private ResourceResponse getDirectoryInfo(String path, Long userId) {
        String objectName = buildObjectName(path, userId);

        minioUtil.directoryExistsOrThrow(objectName, userId);

        return minioUtil.buildResourceResponseForDirectory(
                parentOf(path),
                nameOf(path)
        );
    }

    private ResourceResponse getFileInfo(String path, Long userId) {
        String objectName = buildObjectName(path, userId);

        minioUtil.fileExistsOrThrow(objectName, userId);

        try {
            StatObjectResponse statObject = minioUtil.statObject(objectName);

            return minioUtil.buildResourceResponseForFile(
                    parentOf(path),
                    nameOf(path),
                    statObject.size()
            );
        } catch (Exception e) {
            log.error("Failed to get file info for path='{}', userId={}", path, userId, e);
            throw new RuntimeException("Failed to get file info for path: " + path, e);
        }
    }
}
