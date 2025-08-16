package com.people.job.notification.dto;

import com.people.job.notification.entity.NotificationEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Long id;
    private String recipientUserId;
    private String title;
    private String message;
    private String type;
    private String typeDescription;
    private Boolean isRead;
    private String relatedEntityType;
    private Long relatedEntityId;
    private String actionUrl;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private String timeAgo; // "5분 전", "1시간 전" 등

    // Entity -> DTO 변환
    public static NotificationDTO fromEntity(NotificationEntity entity) {
        return NotificationDTO.builder()
                .id(entity.getId())
                .recipientUserId(entity.getRecipientUserId())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .type(entity.getType().name())
                .typeDescription(entity.getType().getDescription())
                .isRead(entity.getIsRead())
                .relatedEntityType(entity.getRelatedEntityType())
                .relatedEntityId(entity.getRelatedEntityId())
                .actionUrl(entity.getActionUrl())
                .createdAt(entity.getCreatedAt())
                .readAt(entity.getReadAt())
                .timeAgo(calculateTimeAgo(entity.getCreatedAt()))
                .build();
    }

    // 시간 차이 계산 ("5분 전", "1시간 전" 등)
    private static String calculateTimeAgo(LocalDateTime createdAt) {
        if (createdAt == null) return "";

        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(createdAt, now).toMinutes();

        if (minutes < 1) {
            return "방금 전";
        } else if (minutes < 60) {
            return minutes + "분 전";
        } else if (minutes < 1440) { // 24시간
            long hours = minutes / 60;
            return hours + "시간 전";
        } else if (minutes < 10080) { // 7일
            long days = minutes / 1440;
            return days + "일 전";
        } else {
            return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    }

    // 알림 생성 요청 DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateNotificationRequest {
        private String recipientUserId;
        private String title;
        private String message;
        private String type;
        private String relatedEntityType;
        private Long relatedEntityId;
        private String actionUrl;
    }

    // 알림 일괄 처리 요청 DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkActionRequest {
        private java.util.List<Long> notificationIds;
        private String action; // "read", "delete"
    }

    // 알림 통계 DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationStats {
        private Long totalCount;
        private Long unreadCount;
        private Long todayCount;
        private Long weekCount;
        private java.util.Map<String, Long> typeCountMap;
    }

    // 알림 설정 DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationSettings {
        private Boolean emailNotifications;
        private Boolean pushNotifications;
        private Boolean jobApplications;
        private Boolean jobStatusUpdates;
        private Boolean newJobPostings;
        private Boolean systemNotifications;
        private Boolean marketingNotifications;
    }

    // 페이지네이션 응답 DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationPageResponse {
        private java.util.List<NotificationDTO> notifications;
        private Long totalElements;
        private Integer totalPages;
        private Integer currentPage;
        private Integer pageSize;
        private Boolean hasNext;
        private Boolean hasPrevious;
        private Long unreadCount;
    }
}