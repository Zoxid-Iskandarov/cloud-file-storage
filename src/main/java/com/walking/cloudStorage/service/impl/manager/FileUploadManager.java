package com.walking.cloudStorage.service.impl.manager;

import com.walking.cloudStorage.domain.exception.DuplicateException;
import com.walking.cloudStorage.util.MinioUtil;
import com.walking.cloudStorage.web.dto.resource.ResourceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Objects;

import static com.walking.cloudStorage.util.PathUtil.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileUploadManager {
    private final MinioUtil minioUtil;

    public ResourceResponse uploadFile(String path, Long userId, MultipartFile file) {
        String objectName = buildObjectName(path, userId).concat(Objects.requireNonNull(file.getOriginalFilename()));

        if (minioUtil.fileExists(objectName)) {
            throw new DuplicateException("File '%s' by path '%s' already exists"
                    .formatted(file.getOriginalFilename(), path));
        }

        try (InputStream stream = file.getInputStream()) {
            String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();

            minioUtil.putObject(objectName, stream, file.getSize(), contentType);

            return minioUtil.buildResourceResponseForFile(
                    path,
                    nameOf(file.getOriginalFilename()),
                    file.getSize()
            );
        } catch (Exception e) {
            log.error("Failed to upload file: '{}', userId={}, path='{}'", file.getOriginalFilename(), userId, path, e);

            throw new RuntimeException("Failed to upload file: " + file.getOriginalFilename(), e);
        }
    }
}
