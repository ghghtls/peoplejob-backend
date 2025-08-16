package com.people.job.scheduler;

import com.people.job.user.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final PasswordResetService passwordResetService;

    /**
     * 매 시간마다 만료된 토큰들 정리
     */
    @Scheduled(fixedRate = 3600000) // 1시간 = 3600000ms
    public void cleanupExpiredTokens() {
        log.info("만료된 비밀번호 재설정 토큰 정리 시작");
        passwordResetService.cleanupExpiredTokens();
        log.info("만료된 비밀번호 재설정 토큰 정리 완료");
    }
}