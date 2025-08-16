package com.people.job.notification.scheduler;

import com.people.job.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;

    /**
     * 매일 새벽 2시에 오래된 삭제된 알림들 정리
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupOldNotifications() {
        log.info("오래된 알림 정리 작업 시작");
        try {
            notificationService.cleanupOldNotifications();
            log.info("오래된 알림 정리 작업 완료");
        } catch (Exception e) {
            log.error("오래된 알림 정리 작업 실패", e);
        }
    }

    /**
     * 매 시간마다 알림 시스템 상태 체크 (선택사항)
     */
    @Scheduled(fixedRate = 3600000) // 1시간 = 3600000ms
    public void healthCheck() {
        log.debug("알림 시스템 상태 체크");
        // 필요시 알림 시스템 상태 체크 로직 추가
    }
}