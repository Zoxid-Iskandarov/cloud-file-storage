package com.walking.cloudStorage.service.impl.manager;

import com.walking.cloudStorage.util.MinioUtil;
import com.walking.cloudStorage.web.dto.resource.ResourceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;

import static com.walking.cloudStorage.util.PathUtil.*;
import static com.walking.cloudStorage.web.dto.resource.ResourceType.DIRECTORY;

@Slf4j
@Component
@RequiredArgsConstructor
public class DirectoryCreateManager {
    private final MinioUtil minioUtil;

    public ResourceResponse createDirectory(String path, Long userId) {
        String objectName = buildObjectName(path, userId);

        minioUtil.throwIfResourceExists(DIRECTORY, objectName, parentOf(path), nameOf(path));

        try {
            minioUtil.putObject(objectName, new ByteArrayInputStream(new byte[0]));

            return minioUtil.buildResourceResponseForDirectory(
                    parentOf(path),
                    nameOf(path)
            );
        } catch (Exception e) {
            log.error("Failed to create directory: path='{}', userId={}", path, userId, e);
            throw new RuntimeException("Failed to create directory: " + path, e);
        }
    }
}
