package com.people.job.job.repository;

import com.people.job.job.entity.JobopeningEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobopeningRepository extends JpaRepository<JobopeningEntity, Long> {

    Page<JobopeningEntity> findByIsActiveTrueOrderByRegdateDesc(Pageable pageable);

    Optional<JobopeningEntity> findByJobNoAndIsActiveTrue(Long jobNo);

    Page<JobopeningEntity> findByUserNoAndIsActiveTrueOrderByRegdateDesc(Long userNo, Pageable pageable);


    // 게시중인 채용공고만 조회 (일반 사용자용) - 광고 공고 우선 정렬
    @Query("SELECT j FROM JobopeningEntity j WHERE j.status = :status AND j.isActive = true ORDER BY j.isAdvertised DESC, j.regdate DESC")
    Page<JobopeningEntity> findPublishedJobs(@Param("status") JobopeningEntity.JobStatus status, Pageable pageable);

    // 특정 사용자의 모든 채용공고 (상태별)
    Page<JobopeningEntity> findByUserNoAndStatusAndIsActiveTrueOrderByRegdateDesc(
            Long userNo, JobopeningEntity.JobStatus status, Pageable pageable);

    // 특정 사용자의 임시저장 목록
    @Query("SELECT j FROM JobopeningEntity j WHERE j.userNo = :userNo AND j.status = :status AND j.isActive = true ORDER BY j.updatedAt DESC")
    Page<JobopeningEntity> findDraftsByUser(@Param("userNo") Long userNo, @Param("status") JobopeningEntity.JobStatus status, Pageable pageable);

    // 마감일이 지난 게시중인 채용공고들 (스케줄러용)
    @Query("SELECT j FROM JobopeningEntity j WHERE j.status = :status AND j.deadline < :currentDate AND j.isActive = true")
    List<JobopeningEntity> findExpiredJobs(@Param("status") JobopeningEntity.JobStatus status, @Param("currentDate") LocalDate currentDate);

    // 관리자용 - 승인 대기중인 채용공고들
    @Query("SELECT j FROM JobopeningEntity j WHERE j.status = :status AND j.isActive = true ORDER BY j.regdate ASC")
    Page<JobopeningEntity> findPendingJobs(@Param("status") JobopeningEntity.JobStatus status, Pageable pageable);

    // 상태별 개수 조회
    @Query("SELECT COUNT(j) FROM JobopeningEntity j WHERE j.userNo = :userNo AND j.status = :status AND j.isActive = true")
    long countByUserNoAndStatus(@Param("userNo") Long userNo, @Param("status") JobopeningEntity.JobStatus status);

    // 검색 — LIKE (테스트/FULLTEXT 인덱스 없는 환경 호환용)
    @Query("SELECT j FROM JobopeningEntity j WHERE j.status = :status AND j.isActive = true " +
            "AND (j.title LIKE %:keyword% OR j.content LIKE %:keyword% OR j.company LIKE %:keyword%) " +
            "ORDER BY j.isAdvertised DESC, j.regdate DESC")
    Page<JobopeningEntity> searchPublishedJobs(@Param("keyword") String keyword, @Param("status") JobopeningEntity.JobStatus status, Pageable pageable);

    // 검색 — MySQL FULLTEXT ngram BOOLEAN MODE (한국어+영어)
    // MAX_EXECUTION_TIME(2000): 2초 초과 시 예외 → LIKE 폴백 방지용 빠른 실패
    @Query(value =
            "SELECT /*+ MAX_EXECUTION_TIME(2000) */ * FROM jobopening " +
            "WHERE status = 'PUBLISHED' AND isActive = 1 " +
            "AND MATCH(title, content, company) AGAINST(:keyword IN BOOLEAN MODE) " +
            "ORDER BY isAdvertised DESC, regdate DESC",
            countQuery =
            "SELECT /*+ MAX_EXECUTION_TIME(2000) */ COUNT(*) FROM jobopening " +
            "WHERE status = 'PUBLISHED' AND isActive = 1 " +
            "AND MATCH(title, content, company) AGAINST(:keyword IN BOOLEAN MODE)",
            nativeQuery = true)
    Page<JobopeningEntity> fullTextSearchPublishedJobs(@Param("keyword") String keyword, Pageable pageable);

    // 카테고리별 조회 — jobType + location 모두 지정
    @Query("SELECT j FROM JobopeningEntity j WHERE j.status = :status AND j.isActive = true " +
            "AND j.jobType = :jobType AND j.location = :location " +
            "ORDER BY j.isAdvertised DESC, j.regdate DESC")
    Page<JobopeningEntity> findPublishedJobsByCategory(
            @Param("status") JobopeningEntity.JobStatus status,
            @Param("jobType") String jobType,
            @Param("location") String location,
            Pageable pageable);

    // 카테고리별 조회 — jobType만 지정
    @Query("SELECT j FROM JobopeningEntity j WHERE j.status = :status AND j.isActive = true " +
            "AND j.jobType = :jobType " +
            "ORDER BY j.isAdvertised DESC, j.regdate DESC")
    Page<JobopeningEntity> findPublishedJobsByJobType(
            @Param("status") JobopeningEntity.JobStatus status,
            @Param("jobType") String jobType,
            Pageable pageable);

    // 카테고리별 조회 — location만 지정
    @Query("SELECT j FROM JobopeningEntity j WHERE j.status = :status AND j.isActive = true " +
            "AND j.location = :location " +
            "ORDER BY j.isAdvertised DESC, j.regdate DESC")
    Page<JobopeningEntity> findPublishedJobsByLocation(
            @Param("status") JobopeningEntity.JobStatus status,
            @Param("location") String location,
            Pageable pageable);
    List<JobopeningEntity> findByUserNo(Long userNo);

}