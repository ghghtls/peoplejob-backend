package com.people.job.notice.repository;

import com.people.job.notice.entity.NoticeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<NoticeEntity, Long> {

    // 활성화된 공지사항 조회 (최신순)
    List<NoticeEntity> findByIsActiveTrueOrderByRegdateDesc();

    // 활성화된 공지사항 페이징 조회
    Page<NoticeEntity> findByIsActiveTrueOrderByRegdateDesc(Pageable pageable);

    // 중요 공지사항 조회
    List<NoticeEntity> findByIsImportantTrueAndIsActiveTrueOrderByRegdateDesc();

    // 제목으로 검색 (활성화된 것만)
    Page<NoticeEntity> findByIsActiveTrueAndTitleContainingIgnoreCaseOrderByRegdateDesc(
            String title, Pageable pageable);

    // 내용으로 검색 (활성화된 것만)
    Page<NoticeEntity> findByIsActiveTrueAndContentContainingIgnoreCaseOrderByRegdateDesc(
            String content, Pageable pageable);

    // 제목 또는 내용으로 검색
    @Query("SELECT n FROM NoticeEntity n WHERE n.isActive = true AND " +
            "(LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY n.regdate DESC")
    Page<NoticeEntity> findByKeywordInTitleOrContent(@Param("keyword") String keyword, Pageable pageable);

    // 작성자별 공지사항 조회
    List<NoticeEntity> findByWriterOrderByRegdateDesc(String writer);

    // 조회수 업데이트
    @Modifying
    @Query("UPDATE NoticeEntity n SET n.viewCount = n.viewCount + 1 WHERE n.noticeNo = :noticeNo")
    void incrementViewCount(@Param("noticeNo") Long noticeNo);

    // 상위 N개 중요 공지사항 조회
    @Query("SELECT n FROM NoticeEntity n WHERE n.isImportant = true AND n.isActive = true " +
            "ORDER BY n.regdate DESC")
    List<NoticeEntity> findTopImportantNotices(Pageable pageable);

    // 최근 N개 공지사항 조회
    @Query("SELECT n FROM NoticeEntity n WHERE n.isActive = true ORDER BY n.regdate DESC")
    List<NoticeEntity> findRecentNotices(Pageable pageable);

    // 관리자용: 모든 공지사항 조회 (비활성화 포함)
    Page<NoticeEntity> findAllByOrderByRegdateDesc(Pageable pageable);

    // 총 활성화된 공지사항 수
    long countByIsActiveTrue();
}