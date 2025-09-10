package com.walking.cloudStorage.web.dto;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private int status;

    private String error;

    private String message;

    private String path;

    private LocalDateTime timestamp;
}
