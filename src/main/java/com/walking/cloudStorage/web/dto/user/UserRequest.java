package com.walking.cloudStorage.web.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Schema(description = "Request body for user registration and authentication")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {
    @Schema(description = "Unique username of the user", example = "user_1", minLength = 2, maxLength = 100)
    @NotBlank(message = "Username cannot be empty")
    @Length(min = 2, max = 100, message = "Username must be between 2 and 100 characters")
    private String username;

    @Schema(description = "User password. Must contain at least one uppercase, one lowercase letter and one number",
            example = "Password123",
            minLength = 6,
            maxLength = 255)
    @NotBlank(message = "Password cannot be empty")
    @Length(min = 6, max = 255, message = "Password must be at least 6 characters long")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one number")
    private String password;
}
