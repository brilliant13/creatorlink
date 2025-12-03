package com.jung.creatorlink.repository.user;

import com.jung.creatorlink.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    // 로그인에 사용할 이메일 기반 조회
    Optional<User> findByEmail(String email);

}
