package com.walking.cloudStorage.web.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "Standard error response returned by the API")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Short error description", example = "Bad Request")
    private String error;

    @Schema(description = "Detailed error message", example = "Username must be between 2 and 100 characters")
    private String message;

    @Schema(description = "Request path that caused the error", example = "/api/auth/sign-up")
    private String path;

    @Schema(description = "Timestamp of the error", example = "2025-09-21T12:34:56")
    private LocalDateTime timestamp;
}
