package com.jung.creatorlink.controller.auth;

import com.jung.creatorlink.dto.user.UserResponse;
import com.jung.creatorlink.dto.user.UserSignUpRequest;
import com.jung.creatorlink.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "AuthController", description = "회원가입 API")

public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "회원가입", description = "유저 생성")
    public UserResponse signUp(@Valid @RequestBody UserSignUpRequest request) {
        return userService.signUp(request);
    }
}