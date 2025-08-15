package com.people.job.user.repository;

import com.people.job.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {


    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUserid(String userid);

    // 이메일 인증 관련 추가 메서드
    Optional<UserEntity> findByEmailVerificationCode(String emailVerificationCode);
    Optional<UserEntity> findByPasswordResetToken(String passwordResetToken);

    // 이메일 중복 확인
    boolean existsByEmail(String email);

    // 아이디 중복 확인
    boolean existsByUserid(String userid);
}