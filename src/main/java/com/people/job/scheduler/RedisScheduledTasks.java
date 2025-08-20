package com.people.job.scheduler;

import com.people.job.token.TokenCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Redis 관련 스케줄링 작업
 * 정기적인 캐시 정리 및 유지보수 작업
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisScheduledTasks {

    private final TokenCacheService tokenCacheService;

    /**
     * 만료된 토큰 정리 - 매일 새벽 2시 실행
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredTokens() {
        log.info("Starting scheduled cleanup of expired tokens...");
        try {
            tokenCacheService.cleanupExpiredTokens();
            log.info("Scheduled token cleanup completed successfully");
        } catch (Exception e) {
            log.error("Scheduled token cleanup failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Redis 연결 상태 체크 - 매 10분마다 실행
     */
    @Scheduled(fixedRate = 600000) // 10분 = 600,000ms
    public void checkRedisConnection() {
        log.debug("Performing Redis connection health check...");
        try {
            // 단순한 연결 테스트를 위해 토큰 TTL 조회 시도
            long ttl = tokenCacheService.getTokenTTL("test", "connection_check");
            log.debug("Redis connection health check completed - TTL result: {}", ttl);
        } catch (Exception e) {
            log.error("Redis connection health check failed: {}", e.getMessage());
        }
    }

    /**
     * 캐시 통계 로깅 - 매시간 실행
     */
    @Scheduled(cron = "0 0 * * * *")
    public void logCacheStatistics() {
        log.info("=== Redis Cache Statistics ===");
        try {
            // 여기에 캐시 통계 수집 로직 추가 가능
            // 예: 활성 세션 수, 저장된 토큰 수 등
            log.info("Cache statistics logged successfully");
        } catch (Exception e) {
            log.error("Failed to log cache statistics: {}", e.getMessage());
        }
    }
}