package com.walking.cloudStorage.web.controller;

import com.walking.cloudStorage.config.security.UserPrincipal;
import com.walking.cloudStorage.service.StorageService;
import com.walking.cloudStorage.web.dto.resource.ResourceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Tag(name = "Resource", description = "Upload, browse, download, delete, move and search files")
@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {
    private final StorageService storageService;

    @Operation(summary = "Get resource info", description = "Return info about a file or directory by its path")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resource found"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing path"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Resource not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResourceResponse getResourceInfo(@RequestParam String path,
                                            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return storageService.getResourceInfo(path, userPrincipal.getId());
    }

    @Operation(summary = "Delete resource", description = "Delete file or directory by its path")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Resource deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing path"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Resource not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResource(@RequestParam String path,
                               @AuthenticationPrincipal UserPrincipal userPrincipal) {
        storageService.deleteResource(path, userPrincipal.getId());
    }

    @Operation(summary = "Download resource", description = "Downloads a file or directory (as ZIP)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resource downloaded"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing path"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Resource not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/download")
    public void downloadResource(@RequestParam String path,
                                 @AuthenticationPrincipal UserPrincipal userPrincipal,
                                 HttpServletResponse response) {
        storageService.downloadResource(path, userPrincipal.getId(), response);
    }

    @Operation(
            summary = "Move or rename resource",
            description = "Move resource from one path to another, or renames it"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resource moved/renamed"),
            @ApiResponse(responseCode = "400", description = "Invalid path"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Resource not found"),
            @ApiResponse(responseCode = "409", description = "Target resource already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/move")
    public ResourceResponse moveResource(@RequestParam String from,
                                         @RequestParam String to,
                                         @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return storageService.moveResource(from, to, userPrincipal.getId());
    }

    @Operation(summary = "Search resources", description = "Search files and directories by query")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resources found"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing query"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/search")
    public List<ResourceResponse> searchResource(@RequestParam String query,
                                                 @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return storageService.searchResource(query, userPrincipal.getId());
    }

    @Operation(
            summary = "Upload files",
            description = "Upload files (recursively with subfolders) into the specified directory"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Files uploaded"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "409", description = "File already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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
