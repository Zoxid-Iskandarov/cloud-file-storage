package com.walking.cloudStorage.service.impl.manager;

import com.walking.cloudStorage.util.MinioUtil;
import com.walking.cloudStorage.web.dto.resource.ResourceResponse;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.walking.cloudStorage.util.PathUtil.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceSearchManager {
    private final MinioUtil minioUtil;

    public List<ResourceResponse> searchResource(String query, Long userId) {
        query = query.toLowerCase();
        String prefix = userRoot(userId);

        try {
            Iterable<Result<Item>> results = minioUtil.listObjects(prefix, true);
            Map<String, ResourceResponse> foundResources = new LinkedHashMap<>();

            for (Result<Item> result : results) {
                Item item = result.get();
                String objectName = item.objectName();

                if (!objectName.startsWith(prefix)) continue;

                String relativePath = objectName.substring(prefix.length());
                if (relativePath.isEmpty()) continue;

                boolean isDirectory = item.isDir() || isDirectory(relativePath);

                if (isDirectory) {
                    processDirectory(relativePath, query, foundResources);
                } else {
                    processFile(item, relativePath, query, foundResources);
                }
            }

            return new ArrayList<>(foundResources.values());
        } catch (Exception e) {
            log.error("Failed to search resources: query='{}', userId={}", query, userId, e);

            throw new RuntimeException("Failed to search resources for query: " + query, e);
        }
    }

    private void processDirectory(String path, String query, Map<String, ResourceResponse> foundResources) {
        String objectName = nameOf(path);

        if (objectName.toLowerCase().contains(query)) {
            foundResources.putIfAbsent(path, minioUtil.buildResourceResponseForDirectory(
                    parentOf(path),
                    objectName
            ));
        }

        checkParentDirectories(path, query, foundResources);
    }

    private void processFile(Item item, String path, String query, Map<String, ResourceResponse> foundResources) {
        String objectName = nameOf(path);

        if (objectName.toLowerCase().contains(query)) {
            foundResources.putIfAbsent(path, minioUtil.buildResourceResponseForFile(
                    parentOf(path),
                    objectName,
                    item.size()
            ));
        }

        checkParentDirectories(path, query, foundResources);
    }

    private void checkParentDirectories(String path, String query, Map<String, ResourceResponse> foundResources) {
        String currentParent = parentOf(path);

        while (!currentParent.isEmpty()) {
            String objectName = nameOf(currentParent);

            if (objectName.toLowerCase().contains(query)) {
                foundResources.putIfAbsent(currentParent, minioUtil.buildResourceResponseForDirectory(
                        parentOf(currentParent),
                        objectName
                ));
            }

            currentParent = parentOf(currentParent);
        }
    }
}
