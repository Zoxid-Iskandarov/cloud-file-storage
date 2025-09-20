package com.walking.cloudStorage.web.controller;

import com.walking.cloudStorage.domain.exception.BadRequestException;
import com.walking.cloudStorage.service.AuthService;
import com.walking.cloudStorage.web.dto.user.UserRequest;
import com.walking.cloudStorage.web.dto.user.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse singUp(@RequestBody @Validated UserRequest userRequest,
                               BindingResult bindingResult,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(buildErrorMassage(bindingResult));
        }

        return authService.signUp(userRequest, request, response);
    }

    @PostMapping("/sign-in")
    public UserResponse signIn(@RequestBody @Validated UserRequest userRequest,
                               BindingResult bindingResult,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(buildErrorMassage(bindingResult));
        }

        return authService.signIn(userRequest, request, response);
    }

    @PostMapping("/sign-out")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void signOut(HttpServletRequest request) {
        authService.signOut(request);
    }

    private String buildErrorMassage(BindingResult bindingResult) {
        return bindingResult.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));
    }
}
