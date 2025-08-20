package com.people.job.token;

import com.people.job.cache.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 토큰 캐시 서비스
 * JWT 토큰 블랙리스트, 이메일 인증 토큰 등을 관리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCacheService {

    private final CacheService cacheService;

    // 토큰 타입별 프리픽스
    private static final String JWT_BLACKLIST_PREFIX = "jwt_blacklist:";
    private static final String EMAIL_VERIFICATION_PREFIX = "email_verification:";
    private static final String PASSWORD_RESET_PREFIX = "password_reset:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    // 기본 만료 시간
    private static final Duration JWT_BLACKLIST_TTL = Duration.ofDays(1);
    private static final Duration EMAIL_VERIFICATION_TTL = Duration.ofMinutes(30);
    private static final Duration PASSWORD_RESET_TTL = Duration.ofMinutes(15);
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

    /**
     * JWT 토큰을 블랙리스트에 추가
     */
    public void addToBlacklist(String token, Duration expiration) {
        String key = JWT_BLACKLIST_PREFIX + token;
        try {
            cacheService.setString(key, "blacklisted", expiration.toSeconds(), TimeUnit.SECONDS);
            log.info("JWT token added to blacklist - Token: {}...", token.substring(0, Math.min(token.length(), 20)));
        } catch (Exception e) {
            log.error("Failed to add JWT token to blacklist - Error: {}", e.getMessage());
        }
    }

    /**
     * JWT 토큰이 블랙리스트에 있는지 확인
     */
    public boolean isBlacklisted(String token) {
        String key = JWT_BLACKLIST_PREFIX + token;
        try {
            boolean exists = cacheService.hasKey(key);
            log.debug("JWT blacklist check - Token: {}..., Blacklisted: {}",
                    token.substring(0, Math.min(token.length(), 20)), exists);
            return exists;
        } catch (Exception e) {
            log.error("Failed to check JWT blacklist - Error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 이메일 인증 토큰 저장
     */
    public void storeEmailVerificationToken(String email, String token) {
        String key = EMAIL_VERIFICATION_PREFIX + email;
        try {
            cacheService.setString(key, token, EMAIL_VERIFICATION_TTL.toSeconds(), TimeUnit.SECONDS);
            log.info("Email verification token stored - Email: {}", email);
        } catch (Exception e) {
            log.error("Failed to store email verification token - Email: {}, Error: {}", email, e.getMessage());
        }
    }

    /**
     * 이메일 인증 토큰 검증
     */
    public boolean verifyEmailToken(String email, String token) {
        String key = EMAIL_VERIFICATION_PREFIX + email;
        try {
            String storedToken = cacheService.getString(key);
            boolean valid = token != null && token.equals(storedToken);

            if (valid) {
                // 검증 성공 시 토큰 삭제
                cacheService.delete(key);
                log.info("Email verification successful - Email: {}", email);
            } else {
                log.warn("Email verification failed - Email: {}", email);
            }

            return valid;
        } catch (Exception e) {
            log.error("Failed to verify email token - Email: {}, Error: {}", email, e.getMessage());
            return false;
        }
    }

    /**
     * 비밀번호 재설정 토큰 저장
     */
    public void storePasswordResetToken(String email, String token) {
        String key = PASSWORD_RESET_PREFIX + email;
        try {
            cacheService.setString(key, token, PASSWORD_RESET_TTL.toSeconds(), TimeUnit.SECONDS);
            log.info("Password reset token stored - Email: {}", email);
        } catch (Exception e) {
            log.error("Failed to store password reset token - Email: {}, Error: {}", email, e.getMessage());
        }
    }

    /**
     * 비밀번호 재설정 토큰 검증
     */
    public boolean verifyPasswordResetToken(String email, String token) {
        String key = PASSWORD_RESET_PREFIX + email;
        try {
            String storedToken = cacheService.getString(key);
            boolean valid = token != null && token.equals(storedToken);

            if (valid) {
                // 검증 성공 시 토큰 삭제
                cacheService.delete(key);
                log.info("Password reset token verification successful - Email: {}", email);
            } else {
                log.warn("Password reset token verification failed - Email: {}", email);
            }

            return valid;
        } catch (Exception e) {
            log.error("Failed to verify password reset token - Email: {}, Error: {}", email, e.getMessage());
            return false;
        }
    }

    /**
     * 리프레시 토큰 저장
     */
    public void storeRefreshToken(Long userId, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        try {
            cacheService.set(key, refreshToken, REFRESH_TOKEN_TTL);
            log.info("Refresh token stored - UserId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to store refresh token - UserId: {}, Error: {}", userId, e.getMessage());
        }
    }

    /**
     * 리프레시 토큰 검증
     */
    public boolean verifyRefreshToken(Long userId, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        try {
            String storedToken = (String) cacheService.get(key);
            boolean valid = refreshToken != null && refreshToken.equals(storedToken);

            log.debug("Refresh token verification - UserId: {}, Valid: {}", userId, valid);
            return valid;
        } catch (Exception e) {
            log.error("Failed to verify refresh token - UserId: {}, Error: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * 리프레시 토큰 삭제
     */
    public void deleteRefreshToken(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        try {
            boolean deleted = cacheService.delete(key);
            log.info("Refresh token deleted - UserId: {}, Success: {}", userId, deleted);
        } catch (Exception e) {
            log.error("Failed to delete refresh token - UserId: {}, Error: {}", userId, e.getMessage());
        }
    }

    /**
     * 사용자의 모든 토큰 삭제 (로그아웃 시)
     */
    public void deleteAllUserTokens(Long userId) {
        try {
            deleteRefreshToken(userId);

            // 사용자 관련 다른 토큰들도 삭제 가능
            String pattern = "*:" + userId;
            cacheService.deleteByPattern(pattern);

            log.info("All user tokens deleted - UserId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to delete all user tokens - UserId: {}, Error: {}", userId, e.getMessage());
        }
    }

    /**
     * 토큰 TTL 조회
     */
    public long getTokenTTL(String tokenType, String identifier) {
        String key = switch (tokenType.toLowerCase()) {
            case "email_verification" -> EMAIL_VERIFICATION_PREFIX + identifier;
            case "password_reset" -> PASSWORD_RESET_PREFIX + identifier;
            case "refresh_token" -> REFRESH_TOKEN_PREFIX + identifier;
            default -> throw new IllegalArgumentException("Unknown token type: " + tokenType);
        };

        return cacheService.getExpire(key);
    }

    /**
     * 만료된 토큰 정리 (스케줄러에서 호출)
     */
    public void cleanupExpiredTokens() {
        try {
            log.info("Starting expired token cleanup...");

            // 각 프리픽스별로 만료된 키들을 정리
            long deletedCount = 0;

            // JWT 블랙리스트 정리
            deletedCount += cleanupTokensByPrefix(JWT_BLACKLIST_PREFIX);

            // 이메일 인증 토큰 정리
            deletedCount += cleanupTokensByPrefix(EMAIL_VERIFICATION_PREFIX);

            // 비밀번호 재설정 토큰 정리
            deletedCount += cleanupTokensByPrefix(PASSWORD_RESET_PREFIX);

            log.info("Expired token cleanup completed - Deleted: {} tokens", deletedCount);
        } catch (Exception e) {
            log.error("Failed to cleanup expired tokens - Error: {}", e.getMessage());
        }
    }

    /**
     * 특정 프리픽스의 만료된 토큰들 정리
     */
    private long cleanupTokensByPrefix(String prefix) {
        try {
            return cacheService.deleteByPattern(prefix + "*");
        } catch (Exception e) {
            log.error("Failed to cleanup tokens with prefix: {} - Error: {}", prefix, e.getMessage());
            return 0;
        }
    }
}