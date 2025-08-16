package com.people.job.user.service;

import com.people.job.email.service.EmailService;
import com.people.job.user.entity.PasswordResetTokenEntity;
import com.people.job.user.entity.UserEntity;
import com.people.job.user.repository.PasswordResetTokenRepository;
import com.people.job.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 비밀번호 재설정 요청 처리
     */
    public boolean requestPasswordReset(String email) {
        try {
            // 사용자 존재 확인
            Optional<UserEntity> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                log.warn("비밀번호 재설정 요청 - 존재하지 않는 이메일: {}", email);
                return false;
            }

            UserEntity user = userOpt.get();

            // 기존 토큰들을 사용됨으로 표시
            tokenRepository.markAllTokensAsUsedByEmail(email);

            // 새 토큰 생성
            String resetToken = generateResetToken();

            PasswordResetTokenEntity tokenEntity = PasswordResetTokenEntity.builder()
                    .token(resetToken)
                    .email(email)
                    .userId(user.getUserid()) // UserEntity의 userid 필드 사용
                    .build();

            tokenRepository.save(tokenEntity);

            // 이메일 발송
            emailService.sendPasswordResetEmail(email, user.getName(), resetToken); // UserEntity의 name 필드 사용

            log.info("비밀번호 재설정 이메일 발송 성공: {}", email);
            return true;

        } catch (Exception e) {
            log.error("비밀번호 재설정 요청 처리 실패: {}", email, e);
            return false;
        }
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateResetToken(String token) {
        Optional<PasswordResetTokenEntity> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            log.warn("존재하지 않는 비밀번호 재설정 토큰: {}", token);
            return false;
        }

        PasswordResetTokenEntity tokenEntity = tokenOpt.get();
        boolean isValid = tokenEntity.isValid();

        if (!isValid) {
            log.warn("유효하지 않은 비밀번호 재설정 토큰: {} (만료됨 또는 사용됨)", token);
        }

        return isValid;
    }

    /**
     * 비밀번호 재설정 실행
     */
    public boolean resetPassword(String token, String newPassword) {
        try {
            Optional<PasswordResetTokenEntity> tokenOpt = tokenRepository.findByToken(token);

            if (tokenOpt.isEmpty()) {
                log.warn("존재하지 않는 토큰으로 비밀번호 재설정 시도: {}", token);
                return false;
            }

            PasswordResetTokenEntity tokenEntity = tokenOpt.get();

            if (!tokenEntity.isValid()) {
                log.warn("유효하지 않은 토큰으로 비밀번호 재설정 시도: {}", token);
                return false;
            }

            // 사용자 찾기
            Optional<UserEntity> userOpt = userRepository.findByEmail(tokenEntity.getEmail());
            if (userOpt.isEmpty()) {
                log.error("토큰에 연결된 사용자를 찾을 수 없음: {}", tokenEntity.getEmail());
                return false;
            }

            UserEntity user = userOpt.get();

            // 비밀번호 업데이트
            String encodedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedPassword); // UserEntity의 setPassword 메서드 사용
            userRepository.save(user);

            // 토큰을 사용됨으로 표시
            tokenEntity.markAsUsed();
            tokenRepository.save(tokenEntity);

            // 해당 사용자의 다른 토큰들도 모두 사용됨으로 표시
            tokenRepository.markAllTokensAsUsedByEmail(tokenEntity.getEmail());

            log.info("비밀번호 재설정 성공: {}", tokenEntity.getEmail());
            return true;

        } catch (Exception e) {
            log.error("비밀번호 재설정 실행 실패: {}", token, e);
            return false;
        }
    }

    /**
     * 만료된 토큰들 정리 (스케줄러에서 호출)
     */
    public void cleanupExpiredTokens() {
        try {
            tokenRepository.deleteExpiredTokens(LocalDateTime.now());
            log.info("만료된 비밀번호 재설정 토큰들 정리 완료");
        } catch (Exception e) {
            log.error("만료된 토큰 정리 실패", e);
        }
    }

    /**
     * 토큰 정보 조회 (검증용)
     */
    public Optional<PasswordResetTokenEntity> getTokenInfo(String token) {
        return tokenRepository.findByToken(token);
    }

    /**
     * 재설정 토큰 생성
     */
    private String generateResetToken() {
        return UUID.randomUUID().toString().replace("-", "") +
                System.currentTimeMillis();
    }
}