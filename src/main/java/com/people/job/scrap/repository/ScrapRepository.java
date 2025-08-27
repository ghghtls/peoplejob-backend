package com.people.job.scrap.repository;

import com.people.job.scrap.entity.ScrapEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScrapRepository extends JpaRepository<ScrapEntity, Long> {

    // DB 스키마에 맞게 필드명 수정 (jobopeningNo -> jobNo)
    boolean existsByUserNoAndJobNo(Long userNo, Long jobNo);

    List<ScrapEntity> findByUserNo(Long userNo);

    Optional<ScrapEntity> findByUserNoAndJobNo(Long userNo, Long jobNo);

    // 사용자별 스크랩 목록 (페이징, 최신순)
    Page<ScrapEntity> findByUserNoOrderByScrapDateDesc(Long userNo, Pageable pageable);

    // 스크랩 개수 조회
    long countByUserNo(Long userNo);

    // 특정 채용공고의 스크랩 개수
    long countByJobNo(Long jobNo);

    // 사용자별 스크랩 목록을 채용공고 정보와 함께 조회
    @Query("SELECT s FROM ScrapEntity s JOIN JobopeningEntity j ON s.jobNo = j.jobNo " +
            "WHERE s.userNo = :userNo AND j.isActive = true AND j.status = 'PUBLISHED' " +
            "ORDER BY s.scrapDate DESC")
    Page<ScrapEntity> findUserScrapsWithActiveJobs(@Param("userNo") Long userNo, Pageable pageable);

    // 특정 기간 내 스크랩된 항목들 (통계용)
    @Query("SELECT s FROM ScrapEntity s WHERE s.scrapDate >= CURRENT_DATE - :days")
    List<ScrapEntity> findRecentScraps(@Param("days") int days);

    // 인기 채용공고 (스크랩 많이 된 순)
    @Query("SELECT s.jobNo, COUNT(s) as scrapCount FROM ScrapEntity s " +
            "JOIN JobopeningEntity j ON s.jobNo = j.jobNo " +
            "WHERE j.isActive = true AND j.status = 'PUBLISHED' " +
            "GROUP BY s.jobNo ORDER BY scrapCount DESC")
    List<Object[]> findPopularJobsByScrapCount(Pageable pageable);
}