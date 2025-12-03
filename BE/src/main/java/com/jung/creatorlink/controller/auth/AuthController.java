package com.jung.creatorlink.controller.auth;

import com.jung.creatorlink.dto.auth.LoginRequest;
import com.jung.creatorlink.dto.auth.LoginResponse;
import com.jung.creatorlink.dto.user.UserResponse;
import com.jung.creatorlink.dto.user.UserSignUpRequest;
import com.jung.creatorlink.service.auth.AuthService;
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
@Tag(name = "AuthController", description = "회원가입 및 로그인 API")

public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "회원가입", description = "유저 생성")
    public UserResponse signUp(@Valid @RequestBody UserSignUpRequest request) {
        return userService.signUp(request);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인한다. (현재는 토큰 없이 사용자 정보만 반환)")
    public LoginResponse login(@Valid @RequestBody LoginRequest requset) {
        return authService.login(requset);
    }
}