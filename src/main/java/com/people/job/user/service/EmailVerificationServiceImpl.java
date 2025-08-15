package com.people.job.user.service;

import com.people.job.email.service.EmailService;
import com.people.job.user.entity.UserEntity;
import com.people.job.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 이메일 인증코드 생성 및 발송
     */
    @Override
    public boolean sendEmailVerification(String email) {
        try {
            Optional<UserEntity> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                log.warn("이메일 인증 요청 실패 - 사용자를 찾을 수 없음: {}", email);
                return false;
            }

            UserEntity user = userOpt.get();
            if (user.getIsEmailVerified()) {
                log.info("이미 인증된 사용자의 인증 요청: {}", email);
                return false; // 이미 인증된 사용자
            }

            // 6자리 랜덤 인증코드 생성
            String verificationCode = generateRandomCode(6);
            user.setEmailVerificationCode(verificationCode, 30); // 30분 만료
            userRepository.save(user);

            // 이메일 발송
            emailService.sendVerificationEmail(email, user.getName(), verificationCode);

            log.info("이메일 인증코드 발송 완료: {}", email);
            return true;

        } catch (Exception e) {
            log.error("이메일 인증코드 발송 실패: {}", email, e);
            return false;
        }
    }

    /**
     * 이메일 인증 확인
     */
    @Override
    public boolean verifyEmail(String code) {
        try {
            Optional<UserEntity> userOpt = userRepository.findByEmailVerificationCode(code);
            if (userOpt.isEmpty()) {
                log.warn("유효하지 않은 인증코드: {}", code);
                return false;
            }

            UserEntity user = userOpt.get();
            if (user.isEmailVerificationExpired()) {
                log.warn("만료된 인증코드 사용 시도: {} (사용자: {})", code, user.getEmail());
                return false;
            }

            user.setIsEmailVerified(true);
            user.setEmailVerificationCode(null);
            user.setEmailVerificationExpiry(null);
            userRepository.save(user);

            log.info("이메일 인증 완료: {}", user.getEmail());
            return true;

        } catch (Exception e) {
            log.error("이메일 인증 처리 실패: {}", code, e);
            return false;
        }
    }

    /**
     * 비밀번호 재설정 토큰 생성 및 발송
     */
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
            user.setPasswordResetToken(resetToken, 15); // 15분 만료
            userRepository.save(user);

            // 이메일 발송
            emailService.sendPasswordResetEmail(email, user.getName(), resetToken);

            log.info("비밀번호 재설정 이메일 발송 완료: {}", email);
            return true;

        } catch (Exception e) {
            log.error("비밀번호 재설정 이메일 발송 실패: {}", email, e);
            return false;
        }
    }

    /**
     * 비밀번호 재설정
     */
    @Override
    public boolean resetPassword(String token, String newPassword) {
        try {
            Optional<UserEntity> userOpt = userRepository.findByPasswordResetToken(token);
            if (userOpt.isEmpty()) {
                log.warn("유효하지 않은 비밀번호 재설정 토큰: {}", token);
                return false;
            }

            UserEntity user = userOpt.get();
            if (user.isPasswordResetExpired()) {
                log.warn("만료된 비밀번호 재설정 토큰 사용 시도: {} (사용자: {})", token, user.getEmail());
                return false;
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            user.setPasswordResetToken(null);
            user.setPasswordResetExpiry(null);
            userRepository.save(user);

            log.info("비밀번호 재설정 완료: {}", user.getEmail());
            return true;

        } catch (Exception e) {
            log.error("비밀번호 재설정 실패: {}", token, e);
            return false;
        }
    }

    /**
     * 랜덤 숫자 코드 생성
     * @param length 생성할 코드 길이
     * @return 생성된 숫자 코드
     */
    private String generateRandomCode(int length) {
        Random random = new Random();
        int maxValue = (int) Math.pow(10, length);
        int randomValue = random.nextInt(maxValue);
        return String.format("%0" + length + "d", randomValue);
    }
}