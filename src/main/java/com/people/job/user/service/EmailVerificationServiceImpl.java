package com.people.job.user.service;

import com.people.job.email.service.EmailService;
import com.people.job.user.entity.UserEntity;
// ✅ 엔티티/레포지토리 패키지 경로 수정
import com.people.job.user.entity.PasswordResetTokenEntity;
import com.people.job.user.repository.PasswordResetTokenRepository;

import com.people.job.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository tokenRepository;

    private static final int VERIFY_CODE_TTL_MIN = 30;
    private static final int RESET_TOKEN_TTL_MIN = 15;

    /** 이메일 인증코드 생성 및 발송 */
    @Override
    public boolean sendEmailVerification(String email) {
        try {
            Optional<UserEntity> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                log.warn("이메일 인증 요청 실패 - 사용자를 찾을 수 없음: {}", email);
                return false;
            }
            UserEntity user = userOpt.get();

            // 6자리 인증코드 발급 → password_reset_tokens 저장(used=false)
            String verificationCode = generateRandomCode(6);
            PasswordResetTokenEntity token = PasswordResetTokenEntity.builder()
                    .token(verificationCode)
                    .email(email)
                    .userId(user.getUserid()) // users.userid (VARCHAR)
                    .expiryDate(LocalDateTime.now().plusMinutes(VERIFY_CODE_TTL_MIN))
                    .used(false)
                    .build();
            tokenRepository.save(token);

            // 이름 컬럼은 username
            emailService.sendVerificationEmail(email, user.getUsername(), verificationCode);

            log.info("이메일 인증코드 발송 완료: {}", email);
            return true;
        } catch (Exception e) {
            log.error("이메일 인증코드 발송 실패: {}", email, e);
            return false;
        }
    }

    /** 이메일 인증 확인 (코드 검증) */
    @Override
    public boolean verifyEmail(String code) {
        try {
            Optional<PasswordResetTokenEntity> tokenOpt = tokenRepository.findByToken(code);
            if (tokenOpt.isEmpty()) {
                log.warn("유효하지 않은 인증코드: {}", code);
                return false;
            }

            PasswordResetTokenEntity token = tokenOpt.get();
            // 만료 또는 사용됨 체크
            if (Boolean.TRUE.equals(token.getUsed())
                    || token.getExpiryDate() == null
                    || token.getExpiryDate().isBefore(LocalDateTime.now())) {
                log.warn("만료되었거나 사용된 인증코드: {}", code);
                return false;
            }

            // 토큰의 이메일로 사용자 존재만 확인
            if (userRepository.findByEmail(token.getEmail()).isEmpty()) {
                log.warn("토큰의 이메일에 해당하는 사용자가 없음: {}", token.getEmail());
                return false;
            }

            // 토큰 사용 처리
            token.setUsed(true);
            tokenRepository.save(token);

            log.info("이메일 인증 완료: {}", token.getEmail());
            return true;
        } catch (Exception e) {
            log.error("이메일 인증 처리 실패: {}", code, e);
            return false;
        }
    }

    /** 비밀번호 재설정 토큰 생성 및 발송 */
    @Override
    public boolean sendPasswordResetEmail(String email) {
        try {
            Optional<UserEntity> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                log.warn("비밀번호 재설정 요청 실패 - 사용자를 찾을 수 없음: {}", email);
                return false;
            }
            UserEntity user = userOpt.get();

            String resetToken = UUID.randomUUID().toString();
            PasswordResetTokenEntity token = PasswordResetTokenEntity.builder()
                    .token(resetToken)
                    .email(email)
                    .userId(user.getUserid())
                    .expiryDate(LocalDateTime.now().plusMinutes(RESET_TOKEN_TTL_MIN))
                    .used(false)
                    .build();
            tokenRepository.save(token);

            emailService.sendPasswordResetEmail(email, user.getUsername(), resetToken);

            log.info("비밀번호 재설정 이메일 발송 완료: {}", email);
            return true;
        } catch (Exception e) {
            log.error("비밀번호 재설정 이메일 발송 실패: {}", email, e);
            return false;
        }
    }

    /** 비밀번호 재설정 */
    @Override
    public boolean resetPassword(String tokenValue, String newPassword) {
        try {
            Optional<PasswordResetTokenEntity> tokenOpt = tokenRepository.findByToken(tokenValue);
            if (tokenOpt.isEmpty()) {
                log.warn("유효하지 않은 비밀번호 재설정 토큰: {}", tokenValue);
                return false;
            }

            PasswordResetTokenEntity token = tokenOpt.get();
            if (Boolean.TRUE.equals(token.getUsed())
                    || token.getExpiryDate() == null
                    || token.getExpiryDate().isBefore(LocalDateTime.now())) {
                log.warn("만료되었거나 사용된 비밀번호 재설정 토큰: {}", tokenValue);
                return false;
            }

            Optional<UserEntity> userOpt = userRepository.findByEmail(token.getEmail());
            if (userOpt.isEmpty()) {
                log.warn("토큰의 이메일에 해당하는 사용자가 없음: {}", token.getEmail());
                return false;
            }

            UserEntity user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            token.setUsed(true);
            tokenRepository.save(token);

            log.info("비밀번호 재설정 완료: {}", user.getEmail());
            return true;
        } catch (Exception e) {
            log.error("비밀번호 재설정 실패: {}", tokenValue, e);
            return false;
        }
    }

    /** 숫자형 랜덤 코드 생성 */
    private String generateRandomCode(int length) {
        Random random = new Random();
        int maxValue = (int) Math.pow(10, length);
        int randomValue = random.nextInt(maxValue);
        return String.format("%0" + length + "d", randomValue); // length=6 → 000123
    }
}
