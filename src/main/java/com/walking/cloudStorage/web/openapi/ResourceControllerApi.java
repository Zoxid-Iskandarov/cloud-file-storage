package com.walking.cloudStorage.web.openapi;

import com.walking.cloudStorage.config.security.UserPrincipal;
import com.walking.cloudStorage.web.dto.resource.ResourceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Resource", description = "Upload, browse, download, delete, move and search files")
public interface ResourceControllerApi {

    @Operation(summary = "Get resource info", description = "Return info about a file or directory by its path")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resource found"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing path"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Resource not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResourceResponse getResourceInfo(@RequestParam String path, @AuthenticationPrincipal UserPrincipal userPrincipal);

    @Operation(summary = "Delete resource", description = "Delete file or directory by its path")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Resource deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing path"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Resource not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    void deleteResource(@RequestParam String path, @AuthenticationPrincipal UserPrincipal userPrincipal);

    @Operation(summary = "Download resource", description = "Downloads a file or directory (as ZIP)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resource downloaded"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing path"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Resource not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    void downloadResource(@RequestParam String path,
                          @AuthenticationPrincipal UserPrincipal userPrincipal,
                          HttpServletResponse response);

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
    ResourceResponse moveResource(@RequestParam String from, @RequestParam String to,
                                  @AuthenticationPrincipal UserPrincipal userPrincipal);

    @Operation(summary = "Search resources", description = "Search files and directories by query")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resources found"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing query"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    List<ResourceResponse> searchResource(@RequestParam String query,
                                          @AuthenticationPrincipal UserPrincipal userPrincipal);

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
    List<ResourceResponse> uploadFile(@RequestParam(required = false, defaultValue = "") String path,
                                      @RequestParam("object") MultipartFile[] files,
                                      @AuthenticationPrincipal UserPrincipal userPrincipal);
}
