package com.people.job.notification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String recipientUserId; // 알림을 받을 사용자 ID

    @Column(nullable = false)
    private String title; // 알림 제목

    @Column(nullable = false, length = 1000)
    private String message; // 알림 내용

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type; // 알림 타입

    @Column(nullable = false)
    private Boolean isRead = false; // 읽음 여부

    @Column(nullable = false)
    private Boolean isDeleted = false; // 삭제 여부

    private String relatedEntityType; // 연관된 엔티티 타입 (JOB, RESUME, APPLY 등)
    private Long relatedEntityId; // 연관된 엔티티 ID
    private String actionUrl; // 클릭 시 이동할 URL

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime readAt; // 읽은 시간
    private LocalDateTime deletedAt; // 삭제된 시간

    // 알림 타입 enum
    public enum NotificationType {
        JOB_APPLICATION("지원 알림"),
        JOB_STATUS_UPDATE("지원 상태 변경"),
        NEW_JOB_POSTING("새로운 채용공고"),
        RESUME_VIEW("이력서 조회"),
        MESSAGE("메시지"),
        SYSTEM("시스템 알림"),
        PAYMENT("결제 알림"),
        EMAIL_VERIFICATION("이메일 인증"),
        PASSWORD_RESET("비밀번호 재설정"),
        COMPANY_APPROVAL("기업 승인"),
        JOB_EXPIRED("채용공고 만료"),
        INTERVIEW_SCHEDULE("면접 일정");

        private final String description;

        NotificationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 헬퍼 메서드들
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    public void markAsDeleted() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isUnread() {
        return !isRead;
    }

    public boolean isActive() {
        return !isDeleted;
    }

    // 알림 생성을 위한 빌더 패턴 헬퍼
    public static NotificationEntity createJobApplicationNotification(
            String recipientUserId, String applicantName, String jobTitle, Long jobId) {
        return NotificationEntity.builder()
                .recipientUserId(recipientUserId)
                .title("새로운 지원자가 있습니다")
                .message(applicantName + "님이 '" + jobTitle + "' 채용공고에 지원했습니다.")
                .type(NotificationType.JOB_APPLICATION)
                .relatedEntityType("JOB")
                .relatedEntityId(jobId)
                .actionUrl("/jobs/" + jobId + "/applications")
                .build();
    }

    public static NotificationEntity createJobStatusUpdateNotification(
            String recipientUserId, String jobTitle, String status, Long applicationId) {
        return NotificationEntity.builder()
                .recipientUserId(recipientUserId)
                .title("지원 상태가 업데이트되었습니다")
                .message("'" + jobTitle + "' 지원의 상태가 '" + status + "'로 변경되었습니다.")
                .type(NotificationType.JOB_STATUS_UPDATE)
                .relatedEntityType("APPLICATION")
                .relatedEntityId(applicationId)
                .actionUrl("/mypage/applications/" + applicationId)
                .build();
    }

    public static NotificationEntity createNewJobPostingNotification(
            String recipientUserId, String jobTitle, String companyName, Long jobId) {
        return NotificationEntity.builder()
                .recipientUserId(recipientUserId)
                .title("관심 분야의 새로운 채용공고")
                .message(companyName + "에서 '" + jobTitle + "' 채용공고를 올렸습니다.")
                .type(NotificationType.NEW_JOB_POSTING)
                .relatedEntityType("JOB")
                .relatedEntityId(jobId)
                .actionUrl("/jobs/" + jobId)
                .build();
    }

    public static NotificationEntity createSystemNotification(
            String recipientUserId, String title, String message) {
        return NotificationEntity.builder()
                .recipientUserId(recipientUserId)
                .title(title)
                .message(message)
                .type(NotificationType.SYSTEM)
                .build();
    }
}