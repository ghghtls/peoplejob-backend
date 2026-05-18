package com.people.job.apply.repository;

import com.people.job.apply.entity.ApplyEntity;
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
public interface ApplyRepository extends JpaRepository<ApplyEntity, Long> {

    // DB 스키마에 맞게 필드명 수정 (jobopeningNo -> jobNo)
    boolean existsByResumeNoAndJobNo(Long resumeNo, Long jobNo);

    boolean existsByJobNoAndUserNo(Long jobNo, Long userNo);

    List<ApplyEntity> findByResumeNo(Long resumeNo);

    List<ApplyEntity> findByJobNo(Long jobNo);

    Optional<ApplyEntity> findByResumeNoAndJobNo(Long resumeNo, Long jobNo);

    // 사용자별 지원 내역 조회 (페이징)
    Page<ApplyEntity> findByUserNoOrderByApplyDateDesc(Long userNo, Pageable pageable);

    // 특정 사용자의 상태별 지원 내역
    Page<ApplyEntity> findByUserNoAndStatusOrderByApplyDateDesc(Long userNo, String status, Pageable pageable);

    // 특정 채용공고의 지원자 목록 (기업용)
    @Query("SELECT a FROM ApplyEntity a WHERE a.jobNo = :jobNo ORDER BY a.applyDate DESC")
    Page<ApplyEntity> findApplicantsByJobNo(@Param("jobNo") Long jobNo, Pageable pageable);

    // 특정 기업의 모든 채용공고에 대한 지원자 목록
    @Query("""
        SELECT a
        FROM ApplyEntity a
        JOIN JobopeningEntity j ON a.jobNo = j.jobNo
        WHERE j.userNo = :companyUserNo
        ORDER BY a.applyDate DESC
    """)
    Page<ApplyEntity> findApplicantsByCompany(@Param("companyUserNo") Long companyUserNo, Pageable pageable);

    // 중복 지원 확인 (사용자 + 채용공고)
    boolean existsByUserNoAndJobNo(Long userNo, Long jobNo);

    // 특정 사용자가 특정 채용공고에 지원한 내역 조회
    Optional<ApplyEntity> findByUserNoAndJobNo(Long userNo, Long jobNo);

    // 상태별 지원 내역 개수
    long countByUserNoAndStatus(Long userNo, String status);

    // 특정 채용공고의 상태별 지원자 개수
    @Query("SELECT COUNT(a) FROM ApplyEntity a WHERE a.jobNo = :jobNo AND a.status = :status")
    long countByJobNoAndStatus(@Param("jobNo") Long jobNo, @Param("status") String status);

    // ✅ 특정 기업의 신규 지원자 수 (컷오프 날짜 파라미터 사용)
    @Query("""
        SELECT COUNT(a)
        FROM ApplyEntity a
        JOIN JobopeningEntity j ON a.jobNo = j.jobNo
        WHERE j.userNo = :companyUserNo
          AND a.applyDate >= :cutoff
    """)
    long countRecentApplicantsByCompany(@Param("companyUserNo") Long companyUserNo,
                                        @Param("cutoff") LocalDate cutoff);

    // (편의) 최근 7일 헬퍼 — 쓰고 싶을 때 사용
    default long countRecentApplicantsByCompanyLast7Days(Long companyUserNo) {
        return countRecentApplicantsByCompany(companyUserNo, LocalDate.now().minusDays(7));
    }

    // 최근 지원 내역 (개수 제한) — Pageable의 size로 개수 제어
    @Query("SELECT a FROM ApplyEntity a WHERE a.userNo = :userNo ORDER BY a.applyDate DESC")
    List<ApplyEntity> findRecentAppliesByUser(@Param("userNo") Long userNo, Pageable pageable);

    // 사용자 전체 지원 내역 (페이징 없음) — MypageService N+1 제거용
    List<ApplyEntity> findByUserNoOrderByApplyDateDesc(Long userNo);
}
