package com.people.job.ratelimit;

import com.people.job.cache.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Rate Limiting 서비스
 * API 호출 제한, 로그인 시도 제한 등을 관리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final CacheService cacheService;

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final String LOGIN_ATTEMPT_PREFIX = "login_attempt:";
    private static final String API_CALL_PREFIX = "api_call:";

    // 기본 제한 설정
    private static final int DEFAULT_LOGIN_MAX_ATTEMPTS = 5;
    private static final Duration DEFAULT_LOGIN_WINDOW = Duration.ofMinutes(15);
    private static final int DEFAULT_API_MAX_CALLS = 100;
    private static final Duration DEFAULT_API_WINDOW = Duration.ofMinutes(1);

    /**
     * 로그인 시도 횟수 증가
     */
    public boolean incrementLoginAttempt(String identifier) {
        String key = LOGIN_ATTEMPT_PREFIX + identifier;

        try {
            String countStr = cacheService.getString(key);
            int currentCount = countStr != null ? Integer.parseInt(countStr) : 0;
            int newCount = currentCount + 1;

            cacheService.setString(key, String.valueOf(newCount),
                    DEFAULT_LOGIN_WINDOW.toSeconds(), TimeUnit.SECONDS);

            boolean exceeded = newCount > DEFAULT_LOGIN_MAX_ATTEMPTS;
            log.info("Login attempt - Identifier: {}, Count: {}, Exceeded: {}",
                    identifier, newCount, exceeded);

            return !exceeded;
        } catch (Exception e) {
            log.error("Failed to increment login attempt - Identifier: {}, Error: {}",
                    identifier, e.getMessage());
            return true; // 오류 시 허용
        }
    }

    /**
     * 로그인 시도 횟수 확인
     */
    public boolean isLoginAllowed(String identifier) {
        String key = LOGIN_ATTEMPT_PREFIX + identifier;

        try {
            String countStr = cacheService.getString(key);
            int currentCount = countStr != null ? Integer.parseInt(countStr) : 0;

            boolean allowed = currentCount <= DEFAULT_LOGIN_MAX_ATTEMPTS;
            log.debug("Login allowed check - Identifier: {}, Count: {}, Allowed: {}",
                    identifier, currentCount, allowed);

            return allowed;
        } catch (Exception e) {
            log.error("Failed to check login attempts - Identifier: {}, Error: {}",
                    identifier, e.getMessage());
            return true; // 오류 시 허용
        }
    }

    /**
     * 로그인 시도 횟수 초기화 (성공적인 로그인 후)
     */
    public void resetLoginAttempts(String identifier) {
        String key = LOGIN_ATTEMPT_PREFIX + identifier;

        try {
            boolean deleted = cacheService.delete(key);
            log.info("Login attempts reset - Identifier: {}, Success: {}", identifier, deleted);
        } catch (Exception e) {
            log.error("Failed to reset login attempts - Identifier: {}, Error: {}",
                    identifier, e.getMessage());
        }
    }

    /**
     * API 호출 횟수 증가 및 제한 확인
     */
    public boolean checkApiRateLimit(String identifier, int maxCalls, Duration window) {
        String key = API_CALL_PREFIX + identifier;

        try {
            String countStr = cacheService.getString(key);
            int currentCount = countStr != null ? Integer.parseInt(countStr) : 0;

            if (currentCount >= maxCalls) {
                log.warn("API rate limit exceeded - Identifier: {}, Count: {}, Limit: {}",
                        identifier, currentCount, maxCalls);
                return false;
            }

            int newCount = currentCount + 1;
            cacheService.setString(key, String.valueOf(newCount),
                    window.toSeconds(), TimeUnit.SECONDS);

            log.debug("API call counted - Identifier: {}, Count: {}/{}",
                    identifier, newCount, maxCalls);
            return true;
        } catch (Exception e) {
            log.error("Failed to check API rate limit - Identifier: {}, Error: {}",
                    identifier, e.getMessage());
            return true; // 오류 시 허용
        }
    }

    /**
     * 기본 API Rate Limit 확인
     */
    public boolean checkApiRateLimit(String identifier) {
        return checkApiRateLimit(identifier, DEFAULT_API_MAX_CALLS, DEFAULT_API_WINDOW);
    }

    /**
     * 커스텀 Rate Limit 확인
     */
    public boolean checkCustomRateLimit(String category, String identifier,
                                        int maxAttempts, Duration window) {
        String key = RATE_LIMIT_PREFIX + category + ":" + identifier;

        try {
            String countStr = cacheService.getString(key);
            int currentCount = countStr != null ? Integer.parseInt(countStr) : 0;

            if (currentCount >= maxAttempts) {
                log.warn("Custom rate limit exceeded - Category: {}, Identifier: {}, Count: {}, Limit: {}",
                        category, identifier, currentCount, maxAttempts);
                return false;
            }

            int newCount = currentCount + 1;
            cacheService.setString(key, String.valueOf(newCount),
                    window.toSeconds(), TimeUnit.SECONDS);

            log.debug("Custom rate limit counted - Category: {}, Identifier: {}, Count: {}/{}",
                    category, identifier, newCount, maxAttempts);
            return true;
        } catch (Exception e) {
            log.error("Failed to check custom rate limit - Category: {}, Identifier: {}, Error: {}",
                    category, identifier, e.getMessage());
            return true; // 오류 시 허용
        }
    }

    /**
     * Rate Limit 상태 조회
     */
    public RateLimitStatus getRateLimitStatus(String category, String identifier) {
        String key = RATE_LIMIT_PREFIX + category + ":" + identifier;

        try {
            String countStr = cacheService.getString(key);
            int currentCount = countStr != null ? Integer.parseInt(countStr) : 0;
            long ttl = cacheService.getExpire(key);

            return RateLimitStatus.builder()
                    .category(category)
                    .identifier(identifier)
                    .currentCount(currentCount)
                    .remainingTtl(ttl)
                    .build();
        } catch (Exception e) {
            log.error("Failed to get rate limit status - Category: {}, Identifier: {}, Error: {}",
                    category, identifier, e.getMessage());
            return RateLimitStatus.builder()
                    .category(category)
                    .identifier(identifier)
                    .currentCount(0)
                    .remainingTtl(-1)
                    .build();
        }
    }

    /**
     * 특정 카테고리의 Rate Limit 초기화
     */
    public void resetRateLimit(String category, String identifier) {
        String key = RATE_LIMIT_PREFIX + category + ":" + identifier;

        try {
            boolean deleted = cacheService.delete(key);
            log.info("Rate limit reset - Category: {}, Identifier: {}, Success: {}",
                    category, identifier, deleted);
        } catch (Exception e) {
            log.error("Failed to reset rate limit - Category: {}, Identifier: {}, Error: {}",
                    category, identifier, e.getMessage());
        }
    }

    /**
     * 이메일 발송 Rate Limit 확인
     */
    public boolean checkEmailSendLimit(String email) {
        return checkCustomRateLimit("email_send", email, 5, Duration.ofMinutes(10));
    }

    /**
     * 파일 업로드 Rate Limit 확인
     */
    public boolean checkFileUploadLimit(String userId) {
        return checkCustomRateLimit("file_upload", userId, 20, Duration.ofHours(1));
    }

    /**
     * 검색 Rate Limit 확인
     */
    public boolean checkSearchLimit(String identifier) {
        return checkCustomRateLimit("search", identifier, 50, Duration.ofMinutes(5));
    }

    /**
     * Rate Limit 상태 정보 클래스
     */
    @lombok.Builder
    @lombok.Data
    public static class RateLimitStatus {
        private String category;
        private String identifier;
        private int currentCount;
        private long remainingTtl;
    }
}