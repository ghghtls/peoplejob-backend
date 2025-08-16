package com.people.job.notification.repository;

import com.people.job.notification.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    /**
     * 특정 사용자의 활성 알림 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT n FROM NotificationEntity n WHERE n.recipientUserId = :userId AND n.isDeleted = false ORDER BY n.createdAt DESC")
    Page<NotificationEntity> findActiveNotificationsByUserId(@Param("userId") String userId, Pageable pageable);

    /**
     * 특정 사용자의 읽지 않은 알림 조회
     */
    @Query("SELECT n FROM NotificationEntity n WHERE n.recipientUserId = :userId AND n.isRead = false AND n.isDeleted = false ORDER BY n.createdAt DESC")
    List<NotificationEntity> findUnreadNotificationsByUserId(@Param("userId") String userId);

    /**
     * 특정 사용자의 읽지 않은 알림 개수
     */
    @Query("SELECT COUNT(n) FROM NotificationEntity n WHERE n.recipientUserId = :userId AND n.isRead = false AND n.isDeleted = false")
    Long countUnreadNotificationsByUserId(@Param("userId") String userId);

    /**
     * 특정 타입의 알림 조회
     */
    @Query("SELECT n FROM NotificationEntity n WHERE n.recipientUserId = :userId AND n.type = :type AND n.isDeleted = false ORDER BY n.createdAt DESC")
    List<NotificationEntity> findNotificationsByUserIdAndType(@Param("userId") String userId, @Param("type") NotificationEntity.NotificationType type);

    /**
     * 특정 사용자의 모든 알림을 읽음 처리
     */
    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead = true, n.readAt = :readAt, n.updatedAt = :updatedAt WHERE n.recipientUserId = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") String userId, @Param("readAt") LocalDateTime readAt, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 특정 알림들을 읽음 처리
     */
    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead = true, n.readAt = :readAt, n.updatedAt = :updatedAt WHERE n.id IN :ids AND n.recipientUserId = :userId")
    int markAsReadByIds(@Param("ids") List<Long> ids, @Param("userId") String userId, @Param("readAt") LocalDateTime readAt, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 특정 사용자의 모든 알림을 삭제 처리
     */
    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isDeleted = true, n.deletedAt = :deletedAt, n.updatedAt = :updatedAt WHERE n.recipientUserId = :userId AND n.isDeleted = false")
    int markAllAsDeletedByUserId(@Param("userId") String userId, @Param("deletedAt") LocalDateTime deletedAt, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 특정 알림들을 삭제 처리
     */
    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isDeleted = true, n.deletedAt = :deletedAt, n.updatedAt = :updatedAt WHERE n.id IN :ids AND n.recipientUserId = :userId")
    int markAsDeletedByIds(@Param("ids") List<Long> ids, @Param("userId") String userId, @Param("deletedAt") LocalDateTime deletedAt, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 오래된 읽은 알림들 물리적 삭제 (30일 이상 된 것)
     */
    @Modifying
    @Query("DELETE FROM NotificationEntity n WHERE n.isDeleted = true AND n.deletedAt < :cutoffDate")
    int deleteOldDeletedNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * 특정 기간 동안의 알림 조회
     */
    @Query("SELECT n FROM NotificationEntity n WHERE n.recipientUserId = :userId AND n.createdAt BETWEEN :startDate AND :endDate AND n.isDeleted = false ORDER BY n.createdAt DESC")
    List<NotificationEntity> findNotificationsByUserIdAndDateRange(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 연관된 엔티티로 알림 조회
     */
    @Query("SELECT n FROM NotificationEntity n WHERE n.relatedEntityType = :entityType AND n.relatedEntityId = :entityId AND n.isDeleted = false")
    List<NotificationEntity> findNotificationsByRelatedEntity(@Param("entityType") String entityType, @Param("entityId") Long entityId);

    /**
     * 최근 알림 조회 (제한된 개수)
     */
    @Query("SELECT n FROM NotificationEntity n WHERE n.recipientUserId = :userId AND n.isDeleted = false ORDER BY n.createdAt DESC")
    List<NotificationEntity> findRecentNotificationsByUserId(@Param("userId") String userId, Pageable pageable);
}