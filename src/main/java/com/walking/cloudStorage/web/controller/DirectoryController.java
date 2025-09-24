package com.walking.cloudStorage.web.controller;

import com.walking.cloudStorage.config.security.UserPrincipal;
import com.walking.cloudStorage.service.StorageService;
import com.walking.cloudStorage.web.dto.resource.ResourceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Directory", description = "Browse and create directories")
@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
public class DirectoryController {
    private final StorageService storageService;

    @Operation(
            summary = "Get directory content",
            description = "Return non-recursive list of resources in the given directory"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Directory content retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing path"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Directory not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public List<ResourceResponse> getDirectoryContent(@RequestParam(required = false, defaultValue = "") String path,
                                                      @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return storageService.getDirectoryContent(path, userPrincipal.getId());
    }

    @Operation(summary = "Create directory", description = "Create an empty directory at the given path")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Directory created"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing path"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "409", description = "Directory already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceResponse createDirectory(@RequestParam String path,
                                            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return storageService.createDirectory(path, userPrincipal.getId());
    }
}
