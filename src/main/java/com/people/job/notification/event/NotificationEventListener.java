package com.people.job.notification.event;

import com.people.job.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    /**
     * 지원 이벤트 처리
     */
    @Async
    @EventListener
    public void handleJobApplicationEvent(JobApplicationEvent event) {
        try {
            notificationService.createJobApplicationNotification(
                    event.getRecipientUserId(),
                    event.getApplicantName(),
                    event.getJobTitle(),
                    event.getJobId()
            );
            log.info("지원 알림 생성 완료: 사용자 {}, 채용공고 {}", event.getRecipientUserId(), event.getJobId());
        } catch (Exception e) {
            log.error("지원 알림 생성 실패", e);
        }
    }

    /**
     * 지원 상태 변경 이벤트 처리
     */
    @Async
    @EventListener
    public void handleJobStatusUpdateEvent(JobStatusUpdateEvent event) {
        try {
            notificationService.createJobStatusUpdateNotification(
                    event.getRecipientUserId(),
                    event.getJobTitle(),
                    event.getStatus(),
                    event.getApplicationId()
            );
            log.info("지원 상태 변경 알림 생성 완료: 사용자 {}, 지원 {}", event.getRecipientUserId(), event.getApplicationId());
        } catch (Exception e) {
            log.error("지원 상태 변경 알림 생성 실패", e);
        }
    }

    /**
     * 새로운 채용공고 이벤트 처리
     */
    @Async
    @EventListener
    public void handleNewJobPostingEvent(NewJobPostingEvent event) {
        try {
            // 관심 분야가 일치하는 사용자들에게 알림 발송
            for (String userId : event.getInterestedUserIds()) {
                notificationService.createNewJobPostingNotification(
                        userId,
                        event.getJobTitle(),
                        event.getCompanyName(),
                        event.getJobId()
                );
            }
            log.info("새 채용공고 알림 생성 완료: 채용공고 {}, 대상 사용자 {} 명",
                    event.getJobId(), event.getInterestedUserIds().size());
        } catch (Exception e) {
            log.error("새 채용공고 알림 생성 실패", e);
        }
    }

    /**
     * 시스템 이벤트 처리
     */
    @Async
    @EventListener
    public void handleSystemNotificationEvent(SystemNotificationEvent event) {
        try {
            if (event.isForAllUsers()) {
                // 모든 사용자에게 시스템 알림 발송 (배치 처리 권장)
                log.info("전체 사용자 시스템 알림 발송 요청: {}", event.getTitle());
                // 실제 구현에서는 배치 처리로 처리하는 것을 권장
            } else {
                notificationService.createSystemNotification(
                        event.getRecipientUserId(),
                        event.getTitle(),
                        event.getMessage()
                );
                log.info("시스템 알림 생성 완료: 사용자 {}", event.getRecipientUserId());
            }
        } catch (Exception e) {
            log.error("시스템 알림 생성 실패", e);
        }
    }

    // =========================== 이벤트 클래스들 ===========================

    /**
     * 지원 이벤트
     */
    public static class JobApplicationEvent {
        private final String recipientUserId;
        private final String applicantName;
        private final String jobTitle;
        private final Long jobId;

        public JobApplicationEvent(String recipientUserId, String applicantName, String jobTitle, Long jobId) {
            this.recipientUserId = recipientUserId;
            this.applicantName = applicantName;
            this.jobTitle = jobTitle;
            this.jobId = jobId;
        }

        public String getRecipientUserId() { return recipientUserId; }
        public String getApplicantName() { return applicantName; }
        public String getJobTitle() { return jobTitle; }
        public Long getJobId() { return jobId; }
    }

    /**
     * 지원 상태 변경 이벤트
     */
    public static class JobStatusUpdateEvent {
        private final String recipientUserId;
        private final String jobTitle;
        private final String status;
        private final Long applicationId;

        public JobStatusUpdateEvent(String recipientUserId, String jobTitle, String status, Long applicationId) {
            this.recipientUserId = recipientUserId;
            this.jobTitle = jobTitle;
            this.status = status;
            this.applicationId = applicationId;
        }

        public String getRecipientUserId() { return recipientUserId; }
        public String getJobTitle() { return jobTitle; }
        public String getStatus() { return status; }
        public Long getApplicationId() { return applicationId; }
    }

    /**
     * 새로운 채용공고 이벤트
     */
    public static class NewJobPostingEvent {
        private final java.util.List<String> interestedUserIds;
        private final String jobTitle;
        private final String companyName;
        private final Long jobId;

        public NewJobPostingEvent(java.util.List<String> interestedUserIds, String jobTitle, String companyName, Long jobId) {
            this.interestedUserIds = interestedUserIds;
            this.jobTitle = jobTitle;
            this.companyName = companyName;
            this.jobId = jobId;
        }

        public java.util.List<String> getInterestedUserIds() { return interestedUserIds; }
        public String getJobTitle() { return jobTitle; }
        public String getCompanyName() { return companyName; }
        public Long getJobId() { return jobId; }
    }

    /**
     * 시스템 알림 이벤트
     */
    public static class SystemNotificationEvent {
        private final String recipientUserId;
        private final String title;
        private final String message;
        private final boolean forAllUsers;

        public SystemNotificationEvent(String recipientUserId, String title, String message) {
            this.recipientUserId = recipientUserId;
            this.title = title;
            this.message = message;
            this.forAllUsers = false;
        }

        public SystemNotificationEvent(String title, String message, boolean forAllUsers) {
            this.recipientUserId = null;
            this.title = title;
            this.message = message;
            this.forAllUsers = forAllUsers;
        }

        public String getRecipientUserId() { return recipientUserId; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public boolean isForAllUsers() { return forAllUsers; }
    }
}