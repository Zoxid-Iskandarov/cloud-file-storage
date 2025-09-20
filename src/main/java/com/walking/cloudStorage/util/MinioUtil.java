package com.walking.cloudStorage.util;

import com.walking.cloudStorage.domain.exception.ObjectNotFoundException;
import com.walking.cloudStorage.web.dto.resource.ResourceResponse;
import com.walking.cloudStorage.web.dto.resource.ResourceType;
import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;

import static com.walking.cloudStorage.util.PathUtil.*;

@Component
@RequiredArgsConstructor
public class MinioUtil {
    private final MinioClient minioClient;
    @Value("${minio.bucket}")
    private final String bucket;

    public Iterable<Result<Item>> listObjects(String prefix, boolean recursive) {
        return minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucket)
                .prefix(prefix)
                .recursive(recursive)
                .build());
    }

    public StatObjectResponse statObject(String objectName) throws Exception {
        return minioClient.statObject(StatObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .build());
    }

    public void removeObject(String objectName) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .build());
    }

    public InputStream getObject(String objectName) throws Exception {
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .build());
    }

    public void putObject(String objectName, InputStream inputStream, long objectSize, String contentType) throws Exception {
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .stream(inputStream, objectSize, -1)
                .contentType(contentType)
                .build());
    }

    public void putObject(String objectName, InputStream inputStream) throws Exception {
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .stream(inputStream, 0, -1)
                .build());
    }

    public boolean directoryExists(String objectName) {
        return minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucket)
                .prefix(objectName)
                .recursive(true)
                .maxKeys(1)
                .build()).iterator().hasNext();
    }

    public void directoryExistsOrThrow(String objectName, Long userId) {
        if (directoryExists(objectName)) {
            return;
        }

        objectName = objectName.substring(userRoot(userId).length());

        throw new ObjectNotFoundException("Directory '%s' not found".formatted(nameOf(objectName)));
    }

    public boolean fileExists(String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public void fileExistsOrThrow(String objectName, Long userId) {
        if (fileExists(objectName)) {
            return;
        }

        objectName = objectName.substring(userRoot(userId).length());

        throw new ObjectNotFoundException("File '%s' not found".formatted(nameOf(objectName)));
    }

    public ResourceResponse buildResourceResponseForDirectory(String path, String name) {
        return ResourceResponse.builder()
                .path(path)
                .name(name)
                .type(ResourceType.DIRECTORY)
                .build();
    }

    public ResourceResponse buildResourceResponseForFile(String path, String name, Long size) {
        return ResourceResponse.builder()
                .path(path)
                .name(name)
                .size(size)
                .type(ResourceType.FILE)
                .build();
    }
}
