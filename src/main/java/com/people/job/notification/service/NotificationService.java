package com.people.job.notification.service;

import com.people.job.notification.dto.NotificationDTO;
import com.people.job.notification.entity.NotificationEntity;
import com.people.job.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * 알림 생성
     */
    public NotificationEntity createNotification(NotificationDTO.CreateNotificationRequest request) {
        try {
            NotificationEntity notification = NotificationEntity.builder()
                    .recipientUserId(request.getRecipientUserId())
                    .title(request.getTitle())
                    .message(request.getMessage())
                    .type(NotificationEntity.NotificationType.valueOf(request.getType()))
                    .relatedEntityType(request.getRelatedEntityType())
                    .relatedEntityId(request.getRelatedEntityId())
                    .actionUrl(request.getActionUrl())
                    .build();

            NotificationEntity saved = notificationRepository.save(notification);
            log.info("알림 생성 성공: 사용자 {}, 제목 {}", request.getRecipientUserId(), request.getTitle());

            return saved;
        } catch (Exception e) {
            log.error("알림 생성 실패: {}", request, e);
            throw new RuntimeException("알림 생성에 실패했습니다.", e);
        }
    }

    /**
     * 사용자별 알림 목록 조회 (페이지네이션)
     */
    @Transactional(readOnly = true)
    public NotificationDTO.NotificationPageResponse getNotifications(String userId, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<NotificationEntity> notificationPage = notificationRepository.findActiveNotificationsByUserId(userId, pageable);

            List<NotificationDTO> notifications = notificationPage.getContent().stream()
                    .map(NotificationDTO::fromEntity)
                    .collect(Collectors.toList());

            Long unreadCount = notificationRepository.countUnreadNotificationsByUserId(userId);

            return NotificationDTO.NotificationPageResponse.builder()
                    .notifications(notifications)
                    .totalElements(notificationPage.getTotalElements())
                    .totalPages(notificationPage.getTotalPages())
                    .currentPage(page)
                    .pageSize(size)
                    .hasNext(notificationPage.hasNext())
                    .hasPrevious(notificationPage.hasPrevious())
                    .unreadCount(unreadCount)
                    .build();

        } catch (Exception e) {
            log.error("알림 목록 조회 실패: 사용자 {}", userId, e);
            throw new RuntimeException("알림 목록 조회에 실패했습니다.", e);
        }
    }

    /**
     * 읽지 않은 알림 조회
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUnreadNotifications(String userId) {
        try {
            List<NotificationEntity> entities = notificationRepository.findUnreadNotificationsByUserId(userId);
            return entities.stream()
                    .map(NotificationDTO::fromEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("읽지 않은 알림 조회 실패: 사용자 {}", userId, e);
            throw new RuntimeException("읽지 않은 알림 조회에 실패했습니다.", e);
        }
    }

    /**
     * 읽지 않은 알림 개수 조회
     */
    @Transactional(readOnly = true)
    public Long getUnreadCount(String userId) {
        try {
            return notificationRepository.countUnreadNotificationsByUserId(userId);
        } catch (Exception e) {
            log.error("읽지 않은 알림 개수 조회 실패: 사용자 {}", userId, e);
            return 0L;
        }
    }

    /**
     * 특정 알림 읽음 처리
     */
    public boolean markAsRead(Long notificationId, String userId) {
        try {
            Optional<NotificationEntity> notificationOpt = notificationRepository.findById(notificationId);

            if (notificationOpt.isEmpty()) {
                log.warn("존재하지 않는 알림: {}", notificationId);
                return false;
            }

            NotificationEntity notification = notificationOpt.get();

            if (!notification.getRecipientUserId().equals(userId)) {
                log.warn("알림 접근 권한 없음: 알림 {}, 사용자 {}", notificationId, userId);
                return false;
            }

            notification.markAsRead();
            notificationRepository.save(notification);

            log.info("알림 읽음 처리 성공: 알림 {}, 사용자 {}", notificationId, userId);
            return true;

        } catch (Exception e) {
            log.error("알림 읽음 처리 실패: 알림 {}, 사용자 {}", notificationId, userId, e);
            return false;
        }
    }

    /**
     * 여러 알림 일괄 읽음 처리
     */
    public int markMultipleAsRead(List<Long> notificationIds, String userId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            int updated = notificationRepository.markAsReadByIds(notificationIds, userId, now, now);

            log.info("일괄 읽음 처리 성공: {} 개 알림, 사용자 {}", updated, userId);
            return updated;

        } catch (Exception e) {
            log.error("일괄 읽음 처리 실패: 알림들 {}, 사용자 {}", notificationIds, userId, e);
            return 0;
        }
    }

    /**
     * 모든 알림 읽음 처리
     */
    public int markAllAsRead(String userId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            int updated = notificationRepository.markAllAsReadByUserId(userId, now, now);

            log.info("전체 읽음 처리 성공: {} 개 알림, 사용자 {}", updated, userId);
            return updated;

        } catch (Exception e) {
            log.error("전체 읽음 처리 실패: 사용자 {}", userId, e);
            return 0;
        }
    }

    /**
     * 특정 알림 삭제
     */
    public boolean deleteNotification(Long notificationId, String userId) {
        try {
            Optional<NotificationEntity> notificationOpt = notificationRepository.findById(notificationId);

            if (notificationOpt.isEmpty()) {
                log.warn("존재하지 않는 알림: {}", notificationId);
                return false;
            }

            NotificationEntity notification = notificationOpt.get();

            if (!notification.getRecipientUserId().equals(userId)) {
                log.warn("알림 삭제 권한 없음: 알림 {}, 사용자 {}", notificationId, userId);
                return false;
            }

            notification.markAsDeleted();
            notificationRepository.save(notification);

            log.info("알림 삭제 성공: 알림 {}, 사용자 {}", notificationId, userId);
            return true;

        } catch (Exception e) {
            log.error("알림 삭제 실패: 알림 {}, 사용자 {}", notificationId, userId, e);
            return false;
        }
    }

    /**
     * 여러 알림 일괄 삭제
     */
    public int deleteMultipleNotifications(List<Long> notificationIds, String userId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            int updated = notificationRepository.markAsDeletedByIds(notificationIds, userId, now, now);

            log.info("일괄 삭제 성공: {} 개 알림, 사용자 {}", updated, userId);
            return updated;

        } catch (Exception e) {
            log.error("일괄 삭제 실패: 알림들 {}, 사용자 {}", notificationIds, userId, e);
            return 0;
        }
    }

    /**
     * 모든 알림 삭제
     */
    public int deleteAllNotifications(String userId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            int updated = notificationRepository.markAllAsDeletedByUserId(userId, now, now);

            log.info("전체 삭제 성공: {} 개 알림, 사용자 {}", updated, userId);
            return updated;

        } catch (Exception e) {
            log.error("전체 삭제 실패: 사용자 {}", userId, e);
            return 0;
        }
    }

    /**
     * 알림 통계 조회
     */
    @Transactional(readOnly = true)
    public NotificationDTO.NotificationStats getNotificationStats(String userId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
            LocalDateTime weekStart = now.minusDays(7);

            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
            Page<NotificationEntity> allNotifications = notificationRepository.findActiveNotificationsByUserId(userId, pageable);

            Long totalCount = allNotifications.getTotalElements();
            Long unreadCount = notificationRepository.countUnreadNotificationsByUserId(userId);

            List<NotificationEntity> todayNotifications = notificationRepository.findNotificationsByUserIdAndDateRange(
                    userId, todayStart, now);
            Long todayCount = (long) todayNotifications.size();

            List<NotificationEntity> weekNotifications = notificationRepository.findNotificationsByUserIdAndDateRange(
                    userId, weekStart, now);
            Long weekCount = (long) weekNotifications.size();

            Map<String, Long> typeCountMap = allNotifications.getContent().stream()
                    .collect(Collectors.groupingBy(
                            n -> n.getType().name(),
                            Collectors.counting()
                    ));

            return NotificationDTO.NotificationStats.builder()
                    .totalCount(totalCount)
                    .unreadCount(unreadCount)
                    .todayCount(todayCount)
                    .weekCount(weekCount)
                    .typeCountMap(typeCountMap)
                    .build();

        } catch (Exception e) {
            log.error("알림 통계 조회 실패: 사용자 {}", userId, e);
            throw new RuntimeException("알림 통계 조회에 실패했습니다.", e);
        }
    }

    /**
     * 만료된 삭제된 알림들 물리적 삭제 (스케줄러에서 호출)
     */
    public void cleanupOldNotifications() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            int deleted = notificationRepository.deleteOldDeletedNotifications(cutoffDate);
            log.info("오래된 삭제된 알림 정리 완료: {} 개", deleted);
        } catch (Exception e) {
            log.error("오래된 알림 정리 실패", e);
        }
    }

    // =========================== 편의 메서드들 ===========================

    /**
     * 지원 알림 생성
     */
    public void createJobApplicationNotification(String recipientUserId, String applicantName, String jobTitle, Long jobId) {
        NotificationEntity notification = NotificationEntity.createJobApplicationNotification(
                recipientUserId, applicantName, jobTitle, jobId);
        notificationRepository.save(notification);
    }

    /**
     * 지원 상태 업데이트 알림 생성
     */
    public void createJobStatusUpdateNotification(String recipientUserId, String jobTitle, String status, Long applicationId) {
        NotificationEntity notification = NotificationEntity.createJobStatusUpdateNotification(
                recipientUserId, jobTitle, status, applicationId);
        notificationRepository.save(notification);
    }

    /**
     * 새로운 채용공고 알림 생성
     */
    public void createNewJobPostingNotification(String recipientUserId, String jobTitle, String companyName, Long jobId) {
        NotificationEntity notification = NotificationEntity.createNewJobPostingNotification(
                recipientUserId, jobTitle, companyName, jobId);
        notificationRepository.save(notification);
    }

    /**
     * 시스템 알림 생성
     */
    public void createSystemNotification(String recipientUserId, String title, String message) {
        NotificationEntity notification = NotificationEntity.createSystemNotification(
                recipientUserId, title, message);
        notificationRepository.save(notification);
    }
}