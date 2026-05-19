package com.people.job.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 캐시 서비스
 * 다양한 캐시 작업을 위한 공통 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 캐시에 데이터 저장
     */
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            log.debug("Cache set - Key: {}", key);
        } catch (Exception e) {
            log.error("Failed to set cache - Key: {}, Error: {}", key, e.getMessage());
        }
    }

    /**
     * 캐시에 데이터 저장 (만료 시간 포함)
     */
    public void set(String key, Object value, Duration timeout) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout);
            log.debug("Cache set with timeout - Key: {}, Timeout: {}", key, timeout);
        } catch (Exception e) {
            log.error("Failed to set cache with timeout - Key: {}, Error: {}", key, e.getMessage());
        }
    }

    /**
     * 문자열 데이터 저장
     */
    public void setString(String key, String value, long timeout, TimeUnit unit) {
        try {
            stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
            log.debug("String cache set - Key: {}, Timeout: {} {}", key, timeout, unit);
        } catch (Exception e) {
            log.error("Failed to set string cache - Key: {}, Error: {}", key, e.getMessage());
        }
    }

    /**
     * 캐시에서 데이터 조회
     */
    public Object get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            log.debug("Cache get - Key: {}, Found: {}", key, value != null);
            return value;
        } catch (Exception e) {
            log.error("Failed to get cache - Key: {}, Error: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 문자열 데이터 조회
     */
    public String getString(String key) {
        try {
            String value = stringRedisTemplate.opsForValue().get(key);
            log.debug("String cache get - Key: {}, Found: {}", key, value != null);
            return value;
        } catch (Exception e) {
            log.error("Failed to get string cache - Key: {}, Error: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 캐시 데이터 삭제
     */
    public boolean delete(String key) {
        try {
            Boolean result = redisTemplate.delete(key);
            log.debug("Cache delete - Key: {}, Success: {}", key, result);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Failed to delete cache - Key: {}, Error: {}", key, e.getMessage());
            return false;
        }
    }

    /**
     * 캐시 키 존재 여부 확인
     */
    public boolean hasKey(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Failed to check cache key existence - Key: {}, Error: {}", key, e.getMessage());
            return false;
        }
    }

    /**
     * 캐시 만료 시간 설정
     */
    public boolean expire(String key, Duration timeout) {
        try {
            Boolean result = redisTemplate.expire(key, timeout);
            log.debug("Set cache expiration - Key: {}, Timeout: {}, Success: {}", key, timeout, result);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Failed to set cache expiration - Key: {}, Error: {}", key, e.getMessage());
            return false;
        }
    }

    /**
     * 패턴으로 키 검색
     */
    public Set<String> keys(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            log.debug("Keys search - Pattern: {}, Found: {}", pattern, keys != null ? keys.size() : 0);
            return keys;
        } catch (Exception e) {
            log.error("Failed to search keys - Pattern: {}, Error: {}", pattern, e.getMessage());
            return Set.of();
        }
    }

    /**
     * 패턴으로 키 일괄 삭제
     */
    public long deleteByPattern(String pattern) {
        try {
            Set<String> keys = keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                Long deleted = redisTemplate.delete(keys);
                log.debug("Bulk delete - Pattern: {}, Deleted: {}", pattern, deleted);
                return deleted != null ? deleted : 0;
            }
            return 0;
        } catch (Exception e) {
            log.error("Failed to bulk delete - Pattern: {}, Error: {}", pattern, e.getMessage());
            return 0;
        }
    }

    /**
     * 캐시 TTL 조회
     */
    public long getExpire(String key) {
        try {
            Long expire = redisTemplate.getExpire(key);
            return expire != null ? expire : -1;
        } catch (Exception e) {
            log.error("Failed to get cache TTL - Key: {}, Error: {}", key, e.getMessage());
            return -1;
        }
    }

    /**
     * Hash 데이터 저장
     */
    public void hset(String key, String hashKey, Object value) {
        try {
            redisTemplate.opsForHash().put(key, hashKey, value);
            log.debug("Hash set - Key: {}, HashKey: {}", key, hashKey);
        } catch (Exception e) {
            log.error("Failed to set hash - Key: {}, HashKey: {}, Error: {}", key, hashKey, e.getMessage());
        }
    }

    /**
     * Hash 데이터 조회
     */
    public Object hget(String key, String hashKey) {
        try {
            Object value = redisTemplate.opsForHash().get(key, hashKey);
            log.debug("Hash get - Key: {}, HashKey: {}, Found: {}", key, hashKey, value != null);
            return value;
        } catch (Exception e) {
            log.error("Failed to get hash - Key: {}, HashKey: {}, Error: {}", key, hashKey, e.getMessage());
            return null;
        }
    }

    /**
     * Hash 키 삭제
     */
    public boolean hdel(String key, String hashKey) {
        try {
            Long result = redisTemplate.opsForHash().delete(key, hashKey);
            log.debug("Hash delete - Key: {}, HashKey: {}, Success: {}", key, hashKey, result > 0);
            return result != null && result > 0;
        } catch (Exception e) {
            log.error("Failed to delete hash - Key: {}, HashKey: {}, Error: {}", key, hashKey, e.getMessage());
            return false;
        }
    }
}