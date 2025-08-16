package com.people.job.notification.controller;

import com.people.job.notification.dto.NotificationDTO;
import com.people.job.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 알림 목록 조회 (페이지네이션)
     */
    @GetMapping
    public ResponseEntity<NotificationDTO.NotificationPageResponse> getNotifications(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            String userId = getUserIdFromAuth(auth);
            NotificationDTO.NotificationPageResponse response = notificationService.getNotifications(userId, page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("알림 목록 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 읽지 않은 알림 조회
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(Authentication auth) {
        try {
            String userId = getUserIdFromAuth(auth);
            List<NotificationDTO> notifications = notificationService.getUnreadNotifications(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("읽지 않은 알림 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 읽지 않은 알림 개수 조회
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication auth) {
        try {
            String userId = getUserIdFromAuth(auth);
            Long count = notificationService.getUnreadCount(userId);
            return ResponseEntity.ok(Map.of("unreadCount", count));
        } catch (Exception e) {
            log.error("읽지 않은 알림 개수 조회 실패", e);
            return ResponseEntity.ok(Map.of("unreadCount", 0L));
        }
    }

    /**
     * 알림 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<NotificationDTO.NotificationStats> getNotificationStats(Authentication auth) {
        try {
            String userId = getUserIdFromAuth(auth);
            NotificationDTO.NotificationStats stats = notificationService.getNotificationStats(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("알림 통계 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 알림 읽음 처리
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable Long notificationId,
            Authentication auth) {

        try {
            String userId = getUserIdFromAuth(auth);
            boolean success = notificationService.markAsRead(notificationId, userId);

            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "알림을 읽음 처리했습니다."
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "알림을 찾을 수 없습니다."
                ));
            }
        } catch (Exception e) {
            log.error("알림 읽음 처리 실패: {}", notificationId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "서버 오류가 발생했습니다."
            ));
        }
    }

    /**
     * 여러 알림 일괄 읽음 처리
     */
    @PutMapping("/bulk-read")
    public ResponseEntity<Map<String, Object>> markMultipleAsRead(
            @Valid @RequestBody NotificationDTO.BulkActionRequest request,
            Authentication auth) {

        try {
            String userId = getUserIdFromAuth(auth);
            int updated = notificationService.markMultipleAsRead(request.getNotificationIds(), userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", updated + "개의 알림을 읽음 처리했습니다.",
                    "updatedCount", updated
            ));
        } catch (Exception e) {
            log.error("일괄 읽음 처리 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "서버 오류가 발생했습니다."
            ));
        }
    }

    /**
     * 모든 알림 읽음 처리
     */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication auth) {
        try {
            String userId = getUserIdFromAuth(auth);
            int updated = notificationService.markAllAsRead(userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "모든 알림을 읽음 처리했습니다.",
                    "updatedCount", updated
            ));
        } catch (Exception e) {
            log.error("전체 읽음 처리 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "서버 오류가 발생했습니다."
            ));
        }
    }

    /**
     * 특정 알림 삭제
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Map<String, Object>> deleteNotification(
            @PathVariable Long notificationId,
            Authentication auth) {

        try {
            String userId = getUserIdFromAuth(auth);
            boolean success = notificationService.deleteNotification(notificationId, userId);

            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "알림을 삭제했습니다."
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "알림을 찾을 수 없습니다."
                ));
            }
        } catch (Exception e) {
            log.error("알림 삭제 실패: {}", notificationId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "서버 오류가 발생했습니다."
            ));
        }
    }

    /**
     * 여러 알림 일괄 삭제
     */
    @DeleteMapping("/bulk-delete")
    public ResponseEntity<Map<String, Object>> deleteMultipleNotifications(
            @Valid @RequestBody NotificationDTO.BulkActionRequest request,
            Authentication auth) {

        try {
            String userId = getUserIdFromAuth(auth);
            int deleted = notificationService.deleteMultipleNotifications(request.getNotificationIds(), userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", deleted + "개의 알림을 삭제했습니다.",
                    "deletedCount", deleted
            ));
        } catch (Exception e) {
            log.error("일괄 삭제 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "서버 오류가 발생했습니다."
            ));
        }
    }

    /**
     * 모든 알림 삭제
     */
    @DeleteMapping("/delete-all")
    public ResponseEntity<Map<String, Object>> deleteAllNotifications(Authentication auth) {
        try {
            String userId = getUserIdFromAuth(auth);
            int deleted = notificationService.deleteAllNotifications(userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "모든 알림을 삭제했습니다.",
                    "deletedCount", deleted
            ));
        } catch (Exception e) {
            log.error("전체 삭제 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "서버 오류가 발생했습니다."
            ));
        }
    }

    /**
     * 알림 생성 (관리자용 또는 시스템 내부용)
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createNotification(
            @Valid @RequestBody NotificationDTO.CreateNotificationRequest request) {

        try {
            notificationService.createNotification(request);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "알림이 생성되었습니다."
            ));
        } catch (Exception e) {
            log.error("알림 생성 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "서버 오류가 발생했습니다."
            ));
        }
    }

    /**
     * 인증된 사용자의 사용자 ID 추출
     */
    private String getUserIdFromAuth(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("인증되지 않은 사용자입니다.");
        }
        // Authentication에서 사용자 ID 추출
        // 실제 구현에서는 UserDetails나 JWT 토큰에서 사용자 ID를 추출해야 함
        return auth.getName(); // 또는 실제 사용자 ID 추출 로직
    }
}