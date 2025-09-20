package com.walking.cloudStorage.service.impl.manager;

import com.walking.cloudStorage.util.MinioUtil;
import com.walking.cloudStorage.web.dto.resource.ResourceResponse;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.walking.cloudStorage.util.PathUtil.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DirectoryContentManager {
    private final MinioUtil minioUtil;

    public List<ResourceResponse> getDirectoryContent(String path, Long userId) {
        String objectName = buildObjectName(path, userId);

        minioUtil.directoryExistsOrThrow(objectName, userId);

        try {
            Iterable<Result<Item>> results = minioUtil.listObjects(objectName, false);

            Map<String, ResourceResponse> resources = new LinkedHashMap<>();

            for (Result<Item> result : results) {
                Item item = result.get();
                String relativePath = item.objectName().substring(userRoot(userId).length());

                if (relativePath.equals(path)) {
                    continue;
                }

                boolean isDirectory = item.isDir() || relativePath.endsWith("/");

                if (isDirectory) {
                    resources.putIfAbsent(relativePath, minioUtil.buildResourceResponseForDirectory(
                            parentOf(relativePath),
                            nameOf(relativePath))
                    );
                } else {
                    resources.putIfAbsent(relativePath, minioUtil.buildResourceResponseForFile(
                            parentOf(relativePath),
                            nameOf(relativePath),
                            item.size())
                    );
                }
            }

            return new ArrayList<>(resources.values());
        } catch (Exception e) {
            log.error("Failed to fetch directory content: path='{}', userId={}", path, userId, e);

            throw new RuntimeException("Failed to fetch content of directory: " + path, e);
        }
    }
}
