package com.walking.cloudStorage.service.impl;

import com.walking.cloudStorage.domain.exception.BadRequestException;
import com.walking.cloudStorage.service.StorageService;
import com.walking.cloudStorage.service.impl.manager.*;
import com.walking.cloudStorage.web.dto.resource.ResourceResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.walking.cloudStorage.util.PathUtil.*;

@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {
    private final ResourceInfoManager resourceInfoManager;
    private final ResourceDeleteManager resourceDeleteManager;
    private final ResourceDownloadManager resourceDownloadManager;
    private final ResourceMoveManager resourceMoveManager;
    private final ResourceSearchManager resourceSearchManager;
    private final FileUploadManager fileUploadManager;
    private final DirectoryContentManager directoryContentManager;
    private final DirectoryCreateManager directoryCreateManager;

    @Override
    public ResourceResponse getResourceInfo(String path, Long userId) {
        pathNonEmpty(path);
        pathWithoutLeadingSlash(path);

        return resourceInfoManager.getResourceInfo(path, userId);
    }

    @Override
    public void deleteResource(String path, Long userId) {
        pathNonEmpty(path);
        pathWithoutLeadingSlash(path);

        resourceDeleteManager.deleteResource(path, userId);
    }

    @Override
    public void downloadResource(String path, Long userId, HttpServletResponse response) {
        pathNonEmpty(path);
        pathWithoutLeadingSlash(path);

        resourceDownloadManager.downloadResource(path, userId, response);
    }

    @Override
    public ResourceResponse moveResource(String from, String to, Long userId) {
        pathNonEmpty(from, "From");
        pathNonEmpty(to, "To");
        pathWithoutLeadingSlash(from, "From");
        pathWithoutLeadingSlash(to, "To");
        fromAndToNonEqual(from, to);

        boolean fromIsDirectory = isDirectory(from);
        boolean toIsDirectory = isDirectory(to);

        if (fromIsDirectory && !toIsDirectory) {
            throw new BadRequestException("Destination path for a directory must end with a slash");
        }

        if (!fromIsDirectory && toIsDirectory) {
            to = to.concat(nameOf(from));
        }

        return resourceMoveManager.moveResource(from, to, userId);
    }

    @Override
    public List<ResourceResponse> searchResource(String query, Long userId) {
        pathNonEmpty(query, "Search query");

        return resourceSearchManager.searchResource(query, userId);
    }

    @Override
    public ResourceResponse uploadFile(String path, Long userId, MultipartFile file) {
        pathWithoutLeadingSlash(path);
        pathEmptyOrWithTrailingSlash(path);
        pathNonEmpty(file.getOriginalFilename(), "Filename");

        return fileUploadManager.uploadFile(path, userId, file);
    }

    @Override
    public List<ResourceResponse> getDirectoryContent(String path, Long userId) {
        pathWithoutLeadingSlash(path);

        return directoryContentManager.getDirectoryContent(path, userId);
    }

    @Override
    public ResourceResponse createDirectory(String path, Long userId) {
        pathNonEmpty(path);
        pathWithoutLeadingSlash(path);
        pathWithTrailingSlash(path);

        return directoryCreateManager.createDirectory(path, userId);
    }
}
