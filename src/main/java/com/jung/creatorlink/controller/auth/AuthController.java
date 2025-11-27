package com.jung.creatorlink.controller.auth;

import com.jung.creatorlink.dto.user.UserResponse;
import com.jung.creatorlink.dto.user.UserSignUpRequest;
import com.jung.creatorlink.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse signUp(@Valid @RequestBody UserSignUpRequest request) {
        return userService.signUp(request);
    }
}