package com.walking.cloudStorage.web.controller;

import com.walking.cloudStorage.config.security.UserPrincipal;
import com.walking.cloudStorage.service.StorageService;
import com.walking.cloudStorage.web.dto.resource.ResourceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
public class DirectoryController {
    private final StorageService storageService;

    @GetMapping
    public List<ResourceResponse> getDirectoryContent(@RequestParam(required = false, defaultValue = "") String path,
                                                      @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return storageService.getDirectoryContent(path, userPrincipal.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceResponse createDirectory(@RequestParam String path,
                                            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return storageService.createDirectory(path, userPrincipal.getId());
    }
}
