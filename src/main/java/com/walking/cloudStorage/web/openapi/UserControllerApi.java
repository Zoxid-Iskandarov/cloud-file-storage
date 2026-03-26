package com.walking.cloudStorage.web.openapi;

import com.walking.cloudStorage.config.security.UserPrincipal;
import com.walking.cloudStorage.web.dto.user.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "User", description = "Get user profile info")
public interface UserControllerApi {

    @Operation(summary = "Get current user profile", description = "Return info about the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    UserResponse me(@AuthenticationPrincipal UserPrincipal userPrincipal);
}
