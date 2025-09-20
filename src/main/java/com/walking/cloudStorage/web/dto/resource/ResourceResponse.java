package com.walking.cloudStorage.web.dto.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceResponse {
    private String path;

    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long size;

    private ResourceType type;
}
