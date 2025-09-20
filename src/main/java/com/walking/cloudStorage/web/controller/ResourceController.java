package com.walking.cloudStorage.web.controller;

import com.walking.cloudStorage.config.security.UserPrincipal;
import com.walking.cloudStorage.service.StorageService;
import com.walking.cloudStorage.web.dto.resource.ResourceResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {
    private final StorageService storageService;

    @GetMapping
    public ResourceResponse getResourceInfo(@RequestParam String path,
                                            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return storageService.getResourceInfo(path, userPrincipal.getId());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResource(@RequestParam String path,
                               @AuthenticationPrincipal UserPrincipal userPrincipal) {
        storageService.deleteResource(path, userPrincipal.getId());
    }

    @GetMapping("/download")
    public void downloadResource(@RequestParam String path,
                                 @AuthenticationPrincipal UserPrincipal userPrincipal,
                                 HttpServletResponse response) {
        storageService.downloadResource(path, userPrincipal.getId(), response);
    }

    @GetMapping("/move")
    public ResourceResponse moveResource(@RequestParam String from, @RequestParam String to,
                                         @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return storageService.moveResource(from, to, userPrincipal.getId());
    }

    @GetMapping("/search")
    public List<ResourceResponse> searchResource(@RequestParam String query,
                                                 @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return storageService.searchResource(query, userPrincipal.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<ResourceResponse> uploadFile(@RequestParam(required = false, defaultValue = "") String path,
                                             @RequestPart("file") MultipartFile[] files,
                                             @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return Arrays.stream(files)
                .map(file -> storageService.uploadFile(path, userPrincipal.getId(), file))
                .toList();
    }
}
