package com.walking.cloudStorage.service;

import com.walking.cloudStorage.web.dto.resource.ResourceResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StorageService {

    ResourceResponse getResourceInfo(String path, Long userId);

    void deleteResource(String path, Long userId);

    void downloadResource(String path, Long userId, HttpServletResponse response);

    ResourceResponse moveResource(String from, String to, Long userId);

    List<ResourceResponse> searchResource(String query, Long userId);

    ResourceResponse uploadFile(String path, Long userId, MultipartFile file);

    List<ResourceResponse> getDirectoryContent(String path, Long userId);

    ResourceResponse createDirectory(String path, Long userId);
}
