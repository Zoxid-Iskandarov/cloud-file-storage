package com.walking.cloudStorage.service.impl.manager;

import com.walking.cloudStorage.util.MinioUtil;
import io.minio.*;
import io.minio.messages.Item;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.walking.cloudStorage.util.PathUtil.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceDownloadManager {
    private final MinioUtil minioUtil;

    public void downloadResource(String path, Long userId, HttpServletResponse response) {
        if (isDirectory(path)) {
            downloadDirectory(path, userId, response);
        } else {
            downloadFile(path, userId, response);
        }
    }

    private void downloadDirectory(String path, Long userId, HttpServletResponse response) {
        String objectName = buildObjectName(path, userId);

        minioUtil.directoryExistsOrThrow(objectName, userId);

        try {
            addHeaders(response, nameOf(path).concat(".zip"), -1);

            List<String> objects = new ArrayList<>();
            Iterable<Result<Item>> results = minioUtil.listObjects(objectName, true);

            for (Result<Item> result : results) {
                Item item = result.get();

                if (!item.isDir()) {
                    objects.add(item.objectName());
                }
            }

            try (ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
                byte[] buffer = new byte[8192];

                for (String object : objects) {
                    try (InputStream inputStream = minioUtil.getObject(object)) {
                        String relative = object.substring(objectName.length());

                        if (relative.startsWith("/")) {
                            relative = relative.substring(1);
                        }

                        zipOutputStream.putNextEntry(new ZipEntry(relative));

                        int len;
                        while ((len = inputStream.read(buffer)) != -1) {
                            zipOutputStream.write(buffer, 0, len);
                        }

                        zipOutputStream.closeEntry();
                    }
                }

                zipOutputStream.finish();
            }

        } catch (Exception e) {
            log.error("Failed to download directory '{}', userId={}", path, userId, e);

            throw new RuntimeException("Failed to download directory: " + path, e);
        }
    }

    private void downloadFile(String path, Long userId, HttpServletResponse response) {
        String objectName = buildObjectName(path, userId);

        minioUtil.fileExistsOrThrow(objectName, userId);

        try {
            StatObjectResponse statObject = minioUtil.statObject(objectName);

            addHeaders(response, nameOf(path), statObject.size());

            try (InputStream inputStream = minioUtil.getObject(objectName);
                 OutputStream outputStream = response.getOutputStream()) {
                byte[] buffer = new byte[8192];

                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
            }
        } catch (Exception e) {
            log.error("Failed to download file '{}', userId={}", path, userId, e);

            throw new RuntimeException("Failed to download file: " + path, e);
        }
    }

    private void addHeaders(HttpServletResponse response, String fileName, long size) {
        fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replace("+", "%20");

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''%s".formatted(fileName));

        if (size > -1) {
            response.setContentLengthLong(size);
        }
    }
}
