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


    // 게시중인 채용공고만 조회 (일반 사용자용)
    @Query("SELECT j FROM JobopeningEntity j WHERE j.status = 'PUBLISHED' AND j.isActive = true ORDER BY j.regdate DESC")
    Page<JobopeningEntity> findPublishedJobs(Pageable pageable);

    // 특정 사용자의 모든 채용공고 (상태별)
    Page<JobopeningEntity> findByUserNoAndStatusAndIsActiveTrueOrderByRegdateDesc(
            Long userNo, JobopeningEntity.JobStatus status, Pageable pageable);

    // 특정 사용자의 임시저장 목록
    @Query("SELECT j FROM JobopeningEntity j WHERE j.userNo = :userNo AND j.status = 'DRAFT' AND j.isActive = true ORDER BY j.updatedAt DESC")
    Page<JobopeningEntity> findDraftsByUser(@Param("userNo") Long userNo, Pageable pageable);

    // 마감일이 지난 게시중인 채용공고들 (스케줄러용)
    @Query("SELECT j FROM JobopeningEntity j WHERE j.status = 'PUBLISHED' AND j.deadline < :currentDate AND j.isActive = true")
    List<JobopeningEntity> findExpiredJobs(@Param("currentDate") LocalDate currentDate);

    // 관리자용 - 승인 대기중인 채용공고들
    @Query("SELECT j FROM JobopeningEntity j WHERE j.status = 'PENDING' AND j.isActive = true ORDER BY j.regdate ASC")
    Page<JobopeningEntity> findPendingJobs(Pageable pageable);

    // 상태별 개수 조회
    @Query("SELECT COUNT(j) FROM JobopeningEntity j WHERE j.userNo = :userNo AND j.status = :status AND j.isActive = true")
    long countByUserNoAndStatus(@Param("userNo") Long userNo, @Param("status") JobopeningEntity.JobStatus status);

    // 검색 (게시중인 것만)
    @Query("SELECT j FROM JobopeningEntity j WHERE j.status = 'PUBLISHED' AND j.isActive = true " +
            "AND (j.title LIKE %:keyword% OR j.content LIKE %:keyword% OR j.company LIKE %:keyword%) " +
            "ORDER BY j.regdate DESC")
    Page<JobopeningEntity> searchPublishedJobs(@Param("keyword") String keyword, Pageable pageable);

    // 카테고리별 조회 (게시중인 것만)
    @Query("SELECT j FROM JobopeningEntity j WHERE j.status = 'PUBLISHED' AND j.isActive = true " +
            "AND (:jobType IS NULL OR j.jobType = :jobType) " +
            "AND (:location IS NULL OR j.location = :location) " +
            "ORDER BY j.regdate DESC")
    Page<JobopeningEntity> findPublishedJobsByCategory(
            @Param("jobType") String jobType,
            @Param("location") String location,
            Pageable pageable);
    List<JobopeningEntity> findByUserNo(Long userNo);

}