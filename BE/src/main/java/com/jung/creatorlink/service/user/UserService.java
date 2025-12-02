package com.jung.creatorlink.service.user;

import com.jung.creatorlink.domain.user.User;
import com.jung.creatorlink.dto.user.UserResponse;
import com.jung.creatorlink.dto.user.UserSignUpRequest;
import com.jung.creatorlink.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserResponse signUp(UserSignUpRequest request) {

        // 1. 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            // 나중에 커스텀 예외(class EmailAlreadyUsedException)로 빼도 됨
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 2. 비밀번호 해시 (지금은 편의상 그냥 저장, 나중에 PasswordEncoder로 교체)
        String passwordHash = request.getPassword(); // TODO: 나중에 암호화 적용

        // 3. Request DTO -> Entity 변환
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordHash)
                .name(request.getName())
                .createdAt(LocalDateTime.now())
                .build();

        // 4. 저장
        User saved = userRepository.save(user);

        // 5. Entity -> Response DTO 변환
        return UserResponse.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .name(saved.getName())
                .build();
    }
}
