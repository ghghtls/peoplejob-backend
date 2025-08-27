package com.people.job.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import java.util.Properties;

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
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            if (connection == null) {
                return Health.down()
                        .withDetail("status", "Connection failed")
                        .withDetail("error", "Unable to establish connection")
                        .build();
            }

            // PING
            String pingResult = connection.ping();

            Health.Builder healthBuilder = "PONG".equalsIgnoreCase(pingResult)
                    ? Health.up()
                    : Health.unknown();
            healthBuilder.withDetail("ping", pingResult);

            // 추가 정보 수집 (정식 API: Properties)
            try {
                Properties memoryProps = connection.info("memory");
                Properties clientProps = connection.info("clients");

                String usedMemoryHuman = memoryProps != null
                        ? memoryProps.getProperty("used_memory_human", "Unknown")
                        : "Unknown";
                String connectedClients = clientProps != null
                        ? clientProps.getProperty("connected_clients", "Unknown")
                        : "Unknown";

                healthBuilder
                        .withDetail("status", "Connected")
                        .withDetail("memory.used_memory_human", usedMemoryHuman)
                        .withDetail("clients.connected", connectedClients);
            } catch (Exception e) {
                log.warn("Failed to collect additional Redis info: {}", e.getMessage());
                healthBuilder.withDetail("info_collect", "partial");
            }

            return healthBuilder.build();

        } catch (Exception e) {
            log.error("Redis health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("status", "Error")
                    .withDetail("error", e.getMessage())
                    .withDetail("exception", e.getClass().getSimpleName())
                    .build();
        }
    }
}
