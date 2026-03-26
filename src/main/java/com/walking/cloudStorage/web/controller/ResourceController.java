package com.walking.cloudStorage.web.controller;

import com.walking.cloudStorage.config.security.UserPrincipal;
import com.walking.cloudStorage.service.StorageService;
import com.walking.cloudStorage.web.dto.resource.ResourceResponse;
import com.walking.cloudStorage.web.openapi.ResourceControllerApi;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController implements ResourceControllerApi {
    private final StorageService storageService;

    @Override
    @GetMapping
    public ResourceResponse getResourceInfo(@RequestParam String path,
                                            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return storageService.getResourceInfo(path, userPrincipal.getId());
    }

    @Override
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResource(@RequestParam String path,
                               @AuthenticationPrincipal UserPrincipal userPrincipal) {
        storageService.deleteResource(path, userPrincipal.getId());
    }

    @Override
    @GetMapping("/download")
    public void downloadResource(@RequestParam String path,
                                 @AuthenticationPrincipal UserPrincipal userPrincipal,
                                 HttpServletResponse response) {
        storageService.downloadResource(path, userPrincipal.getId(), response);
    }

    @Override
    @GetMapping("/move")
    public ResourceResponse moveResource(@RequestParam String from,
                                         @RequestParam String to,
                                         @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return storageService.moveResource(from, to, userPrincipal.getId());
    }

    @Override
    @GetMapping("/search")
    public List<ResourceResponse> searchResource(@RequestParam String query,
                                                 @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return storageService.searchResource(query, userPrincipal.getId());
    }

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public List<ResourceResponse> uploadFile(@RequestParam(required = false, defaultValue = "") String path,
                                             @RequestParam("object") MultipartFile[] files,
                                             @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return Arrays.stream(files)
                .map(file -> storageService.uploadFile(path, userPrincipal.getId(), file))
                .toList();
    }
}
