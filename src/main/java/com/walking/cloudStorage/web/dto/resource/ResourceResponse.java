package com.walking.cloudStorage.web.dto.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "Represents a file or directory in the storage")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceResponse {
    @Schema(description = "Path to the parent folder where the resource is located", example = "folder1/folder2/")
    private String path;

    @Schema(description = "Name of the resource (file or directory)", example = "file.txt")
    private String name;

    @Schema(description = "Size of the file in bytes (not present for directories)", example = "123")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long size;

    @Schema(description = "Type of the resource: FILE or DIRECTORY", example = "FILE")
    private ResourceType type;
}
