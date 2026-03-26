package com.walking.cloudStorage.web.openapi;

import com.walking.cloudStorage.web.dto.user.UserRequest;
import com.walking.cloudStorage.web.dto.user.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Authentication", description = "User registration, login and logout")
public interface AuthControllerApi {

    @Operation(
            summary = "Register new user",
            description = "Create a new user and starts a session and return user details."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully registered"),
            @ApiResponse(responseCode = "400", description = "Validation exception"),
            @ApiResponse(responseCode = "409", description = "Username already taken"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    UserResponse singUp(@RequestBody @Validated UserRequest userRequest,
                        BindingResult bindingResult,
                        HttpServletRequest request,
                        HttpServletResponse response);

    @Operation(
            summary = "Authenticate user",
            description = "Log in a user with provided credentials and return user details."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully authenticated"),
            @ApiResponse(responseCode = "400", description = "Validation exception"),
            @ApiResponse(responseCode = "401", description = "Invalid username or password"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    UserResponse signIn(@RequestBody @Validated UserRequest userRequest,
                        BindingResult bindingResult,
                        HttpServletRequest request,
                        HttpServletResponse response);

    @Operation(summary = "Logout user", description = "End the current user session.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully logged out"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    void signOut(HttpServletRequest request);
}
