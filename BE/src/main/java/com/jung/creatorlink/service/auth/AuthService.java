package com.jung.creatorlink.service.auth;

import com.jung.creatorlink.domain.user.User;
import com.jung.creatorlink.dto.auth.LoginRequest;
import com.jung.creatorlink.dto.auth.LoginResponse;
import com.jung.creatorlink.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;

    //로그인 처리
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        //  현재는 비밀번호를 평문으로 비교 (학습용)
        //  나중에 BCrypt로 암호화 & matches()로 변경할 것.
        if (!user.getPasswordHash().equals(request.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return LoginResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }
}
