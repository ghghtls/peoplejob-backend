package com.people.job.scrap.repository;

import com.people.job.scrap.entity.ScrapEntity;
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
public interface ScrapRepository extends JpaRepository<ScrapEntity, Long> {

    // 스크랩 여부
    boolean existsByUserNoAndJobNo(Long userNo, Long jobNo);

    // 사용자별 스크랩 조회
    List<ScrapEntity> findByUserNo(Long userNo);

    // 특정 공고 스크랩 단건 조회
    Optional<ScrapEntity> findByUserNoAndJobNo(Long userNo, Long jobNo);

    // 사용자별 스크랩 목록 (페이징, 최신순)
    Page<ScrapEntity> findByUserNoOrderByScrapDateDesc(Long userNo, Pageable pageable);

    // 스크랩 개수 조회
    long countByUserNo(Long userNo);

    // 특정 채용공고의 스크랩 개수
    long countByJobNo(Long jobNo);

    // 사용자별 스크랩 목록을 채용공고 정보와 함께 조회 (활성/게시중 공고만)
    @Query("""
        SELECT s
        FROM ScrapEntity s
        JOIN JobopeningEntity j ON s.jobNo = j.jobNo
        WHERE s.userNo = :userNo
          AND j.isActive = true
          AND j.status = 'PUBLISHED'
        ORDER BY s.scrapDate DESC
    """)
    Page<ScrapEntity> findUserScrapsWithActiveJobs(@Param("userNo") Long userNo, Pageable pageable);

    // ✅ 최근 스크랩: LocalDate 컷오프(타입 일치)로 고정 — 부팅 오류의 원인 제거
    @Query("""
        SELECT s
        FROM ScrapEntity s
        WHERE s.userNo = :userNo
          AND s.scrapDate >= :cutoff
        ORDER BY s.scrapDate DESC
    """)
    List<ScrapEntity> findRecentScraps(@Param("userNo") Long userNo,
                                       @Param("cutoff") LocalDate cutoff);

    // (편의) 최근 7일
    default List<ScrapEntity> findRecentScrapsLast7Days(Long userNo) {
        return findRecentScraps(userNo, LocalDate.now().minusDays(7));
    }

    // 인기 채용공고 (스크랩 많이 된 순)
    @Query("""
        SELECT s.jobNo, COUNT(s) AS scrapCount
        FROM ScrapEntity s
        JOIN JobopeningEntity j ON s.jobNo = j.jobNo
        WHERE j.isActive = true
          AND j.status = 'PUBLISHED'
        GROUP BY s.jobNo
        ORDER BY scrapCount DESC
    """)
    List<Object[]> findPopularJobsByScrapCount(Pageable pageable);
}
