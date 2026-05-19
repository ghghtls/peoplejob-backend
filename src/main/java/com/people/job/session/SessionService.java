package com.people.job.session;

import com.people.job.cache.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * 세션 관리 서비스
 * Redis를 이용한 사용자 세션 관리
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class SessionService {

    private final CacheService cacheService;

    private static final String SESSION_PREFIX = "session:";
    private static final String USER_SESSION_PREFIX = "user_session:";
    private static final Duration DEFAULT_SESSION_TIMEOUT = Duration.ofHours(24);

    /**
     * 세션 생성
     */
    public String createSession(Long userId, Object sessionData) {
        String sessionId = UUID.randomUUID().toString();
        String sessionKey = SESSION_PREFIX + sessionId;
        String userSessionKey = USER_SESSION_PREFIX + userId;

        try {
            // 세션 데이터 저장
            cacheService.set(sessionKey, sessionData, DEFAULT_SESSION_TIMEOUT);

            // 사용자별 세션 ID 저장 (중복 로그인 방지)
            cacheService.set(userSessionKey, sessionId, DEFAULT_SESSION_TIMEOUT);

            log.info("Session created - SessionId: {}, UserId: {}", sessionId, userId);
            return sessionId;
        } catch (Exception e) {
            log.error("Failed to create session - UserId: {}, Error: {}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * 세션 조회
     */
    public Object getSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return null;
        }

        String sessionKey = SESSION_PREFIX + sessionId;
        Object sessionData = cacheService.get(sessionKey);

        if (sessionData != null) {
            // 세션 만료 시간 연장
            cacheService.expire(sessionKey, DEFAULT_SESSION_TIMEOUT);
            log.debug("Session retrieved and extended - SessionId: {}", sessionId);
        } else {
            log.debug("Session not found - SessionId: {}", sessionId);
        }

        return sessionData;
    }

    /**
     * 세션 업데이트
     */
    public boolean updateSession(String sessionId, Object sessionData) {
        if (sessionId == null || sessionId.isEmpty()) {
            return false;
        }

        String sessionKey = SESSION_PREFIX + sessionId;

        try {
            if (cacheService.hasKey(sessionKey)) {
                cacheService.set(sessionKey, sessionData, DEFAULT_SESSION_TIMEOUT);
                log.debug("Session updated - SessionId: {}", sessionId);
                return true;
            } else {
                log.debug("Session not found for update - SessionId: {}", sessionId);
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to update session - SessionId: {}, Error: {}", sessionId, e.getMessage());
            return false;
        }
    }

    /**
     * 세션 삭제
     */
    public boolean deleteSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return false;
        }

        String sessionKey = SESSION_PREFIX + sessionId;

        try {
            boolean deleted = cacheService.delete(sessionKey);
            if (deleted) {
                log.info("Session deleted - SessionId: {}", sessionId);
            } else {
                log.debug("Session not found for deletion - SessionId: {}", sessionId);
            }
            return deleted;
        } catch (Exception e) {
            log.error("Failed to delete session - SessionId: {}, Error: {}", sessionId, e.getMessage());
            return false;
        }
    }

    /**
     * 사용자의 모든 세션 삭제
     */
    public boolean deleteUserSessions(Long userId) {
        String userSessionKey = USER_SESSION_PREFIX + userId;

        try {
            String sessionId = (String) cacheService.get(userSessionKey);
            if (sessionId != null) {
                deleteSession(sessionId);
            }

            boolean deleted = cacheService.delete(userSessionKey);
            log.info("User sessions deleted - UserId: {}, Success: {}", userId, deleted);
            return deleted;
        } catch (Exception e) {
            log.error("Failed to delete user sessions - UserId: {}, Error: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * 세션 유효성 검사
     */
    public boolean isValidSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return false;
        }

        String sessionKey = SESSION_PREFIX + sessionId;
        boolean exists = cacheService.hasKey(sessionKey);

        log.debug("Session validation - SessionId: {}, Valid: {}", sessionId, exists);
        return exists;
    }

    /**
     * 세션 만료 시간 연장
     */
    public boolean extendSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return false;
        }

        String sessionKey = SESSION_PREFIX + sessionId;

        try {
            boolean extended = cacheService.expire(sessionKey, DEFAULT_SESSION_TIMEOUT);
            log.debug("Session extended - SessionId: {}, Success: {}", sessionId, extended);
            return extended;
        } catch (Exception e) {
            log.error("Failed to extend session - SessionId: {}, Error: {}", sessionId, e.getMessage());
            return false;
        }
    }

    /**
     * 세션 남은 시간 조회
     */
    public long getSessionTTL(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return -1;
        }

        String sessionKey = SESSION_PREFIX + sessionId;
        return cacheService.getExpire(sessionKey);
    }

    /**
     * 사용자의 현재 세션 ID 조회
     */
    public String getUserSessionId(Long userId) {
        String userSessionKey = USER_SESSION_PREFIX + userId;
        return (String) cacheService.get(userSessionKey);
    }
}