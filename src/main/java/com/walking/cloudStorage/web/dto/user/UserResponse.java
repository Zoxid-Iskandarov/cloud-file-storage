package com.walking.cloudStorage.web.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "Response body with user profile info")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    @Schema(description = "Unique user ID", example = "1")
    private Long id;

    @Schema(description = "Username of the user", example = "user_1")
    private String username;

    @Schema(description = "Date and time when the user was created", example = "2025-09-21T12:34:56")
    private LocalDateTime created;
}
