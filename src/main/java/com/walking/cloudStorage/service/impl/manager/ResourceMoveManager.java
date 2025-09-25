package com.walking.cloudStorage.service.impl.manager;

import com.walking.cloudStorage.util.MinioUtil;
import com.walking.cloudStorage.web.dto.resource.ResourceResponse;
import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.walking.cloudStorage.util.PathUtil.*;
import static com.walking.cloudStorage.web.dto.resource.ResourceType.DIRECTORY;
import static com.walking.cloudStorage.web.dto.resource.ResourceType.FILE;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceMoveManager {
    private final MinioUtil minioUtil;

    public ResourceResponse moveResource(String from, String to, Long userId) {
        return isDirectory(from) ? moveDirectory(from, to, userId) : moveFile(from, to, userId);
    }

    private ResourceResponse moveDirectory(String from, String to, Long userId) {
        String sourcePrefix = buildObjectName(from, userId);
        String targetPrefix = buildObjectName(to, userId);

        minioUtil.directoryExistsOrThrow(sourcePrefix, userId);
        minioUtil.throwIfResourceExists(DIRECTORY, targetPrefix, parentOf(to), nameOf(to));

        List<String> copiedObjectNames = new ArrayList<>();
        Iterable<Result<Item>> results = minioUtil.listObjects(sourcePrefix, true);

        boolean hasObjects = false;

        try {
            for (Result<Item> result : results) {
                String objectName = result.get().objectName();

                if (objectName.endsWith("/")) continue;

                hasObjects = true;

                String newObjectName = targetPrefix.concat(objectName.substring(sourcePrefix.length()));

                copyFile(objectName, newObjectName);
                copiedObjectNames.add(newObjectName);
            }

            if (!hasObjects) {
                minioUtil.putObject(targetPrefix, new ByteArrayInputStream(new byte[0]));
            }

            Iterable<Result<Item>> toDelete = minioUtil.listObjects(sourcePrefix, true);

            for (Result<Item> result : toDelete) {
                minioUtil.removeObject(result.get().objectName());
            }

            return minioUtil.buildResourceResponseForDirectory(
                    parentOf(to),
                    nameOf(to)
            );
        } catch (Exception e) {
            log.error("Failed to move directory: from='{}' to='{}', userId={}. Rolling back {} objects",
                    from, to, userId, copiedObjectNames.size(), e);
            removeCopiedObjects(copiedObjectNames);

            throw new RuntimeException("Failed to move directory: " + from + " -> " + to, e);
        }
    }

    private ResourceResponse moveFile(String from, String to, Long userId) {
        String sourcePrefix = buildObjectName(from, userId);
        String targetPrefix = buildObjectName(to, userId);

        minioUtil.fileExistsOrThrow(sourcePrefix, userId);
        minioUtil.throwIfResourceExists(FILE, targetPrefix, parentOf(to), nameOf(from));

        try {
            long size = copyFile(sourcePrefix, targetPrefix);
            minioUtil.removeObject(sourcePrefix);

            return minioUtil.buildResourceResponseForFile(
                    parentOf(to),
                    nameOf(to),
                    size
            );
        } catch (Exception e) {
            log.error("Failed to move file: from='{}' to='{}', userId={}", from, to, userId, e);
            throw new RuntimeException("Failed to move file: " + from + " -> " + to, e);
        }
    }

    private long copyFile(String sourcePrefix, String targetPrefix) {
        try {
            StatObjectResponse statObject = minioUtil.statObject(sourcePrefix);

            long size = statObject.size();
            String contentType = statObject.contentType() == null
                    ? "application/octet-stream" : statObject.contentType();

            try (InputStream inputStream = minioUtil.getObject(sourcePrefix)) {
                minioUtil.putObject(targetPrefix, inputStream, size, contentType);
            }

            return size;
        } catch (Exception e) {
            log.error("Failed to copy file: {} -> {}", sourcePrefix, targetPrefix, e);
            throw new RuntimeException("Failed to copy file: " + sourcePrefix + " -> " + targetPrefix, e);
        }
    }

    private void removeCopiedObjects(List<String> copiedObjectNames) {
        try {
            for (String objectName : copiedObjectNames) {
                minioUtil.removeObject(objectName);
            }
        } catch (Exception e) {
            log.error("Rollback failed while removing copied objects: {}", copiedObjectNames, e);
            throw new RuntimeException("Rollback failed for copied objects", e);
        }
    }
}
