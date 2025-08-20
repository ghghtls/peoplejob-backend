package com.people.job.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * Redis 헬스 체크 인디케이터
 * Spring Boot Actuator를 통한 Redis 상태 모니터링
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    public Health health() {
        try {
            RedisConnection connection = redisConnectionFactory.getConnection();

            if (connection != null) {
                // PING 명령으로 연결 확인
                String pingResult = connection.ping();

                // 연결 정보 수집
                Health.Builder healthBuilder = Health.up()
                        .withDetail("ping", pingResult)
                        .withDetail("status", "Connected");

                // 추가 정보 수집
                try {
                    // 메모리 정보
                    String memoryInfo = connection.info("memory").toString();
                    healthBuilder.withDetail("memory_info", parseMemoryInfo(memoryInfo));

                    // 클라이언트 연결 수
                    String clientInfo = connection.info("clients").toString();
                    healthBuilder.withDetail("client_info", parseClientInfo(clientInfo));

                } catch (Exception e) {
                    log.warn("Failed to collect additional Redis info: {}", e.getMessage());
                }

                connection.close();
                return healthBuilder.build();
            } else {
                return Health.down()
                        .withDetail("status", "Connection failed")
                        .withDetail("error", "Unable to establish connection")
                        .build();
            }
        } catch (Exception e) {
            log.error("Redis health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("status", "Error")
                    .withDetail("error", e.getMessage())
                    .withDetail("exception", e.getClass().getSimpleName())
                    .build();
        }
    }

    /**
     * 메모리 정보 파싱
     */
    private String parseMemoryInfo(String memoryInfo) {
        try {
            // used_memory 추출
            String[] lines = memoryInfo.split("\r\n");
            for (String line : lines) {
                if (line.startsWith("used_memory_human:")) {
                    return line.split(":")[1];
                }
            }
            return "Unknown";
        } catch (Exception e) {
            return "Parse error";
        }
    }

    /**
     * 클라이언트 정보 파싱
     */
    private String parseClientInfo(String clientInfo) {
        try {
            // connected_clients 추출
            String[] lines = clientInfo.split("\r\n");
            for (String line : lines) {
                if (line.startsWith("connected_clients:")) {
                    return line.split(":")[1] + " connections";
                }
            }
            return "Unknown";
        } catch (Exception e) {
            return "Parse error";
        }
    }
}