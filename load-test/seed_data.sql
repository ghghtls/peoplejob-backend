-- ============================================================
-- PeopleJob 부하테스트용 더미 데이터 삽입 스크립트
-- 대상 DB: peoplejob
-- 삽입 건수: 채용공고 10만 건 + 지원 5만 건
-- 실행: MySQL에서 source 명령 또는 MySQL Workbench에서 실행
-- ============================================================

USE peoplejob;

-- ── 채용공고 10만 건 ────────────────────────────────────────
-- 기존 더미 데이터 초기화 (주의: 운영 데이터가 없을 때만 실행)
-- DELETE FROM jobopening WHERE userNo = 1;

INSERT INTO jobopening (
    title, content, company, location, jobType,
    salary, workType, experience, education,
    deadline, status, isActive, userNo,
    regdate, updatedAt, viewCount, isAdvertised
)
SELECT
    CONCAT(
        ELT(FLOOR(RAND() * 10) + 1,
            'Java 백엔드', 'React 프론트엔드', 'Python 데이터', 'iOS 개발자', 'Android 개발자',
            '서버 DevOps', 'UI/UX 디자이너', '마케팅 담당자', '영업 관리자', '인사 담당자'
        ),
        ' 개발자 모집 - ', seq
    ),
    CONCAT(
        '<p>저희 회사에서 ', seq, '번 포지션 인재를 모집합니다.</p>',
        '<p>업무: 서비스 개발 및 운영, 코드 리뷰, 기술 문서 작성</p>',
        '<p>우대사항: Spring Boot, React, Docker 경험자</p>'
    ),
    CONCAT(
        ELT(FLOOR(RAND() * 10) + 1,
            '카카오', '네이버', '라인', '쿠팡', '배달의민족',
            '토스', '당근마켓', '야놀자', '직방', '마켓컬리'
        ),
        ' 계열 ', FLOOR(RAND() * 100) + 1, '호'
    ),
    ELT(FLOOR(RAND() * 6) + 1, '서울', '부산', '대구', '인천', '광주', '대전'),
    ELT(FLOOR(RAND() * 6) + 1, 'IT개발', '영업', '마케팅', '디자인', '인사', '경영지원'),
    (FLOOR(RAND() * 8) + 3) * 500,                         -- 1500 ~ 4000 (만원 단위)
    ELT(FLOOR(RAND() * 3) + 1, '정규직', '계약직', '인턴'),
    ELT(FLOOR(RAND() * 3) + 1, '신입', '경력', '무관'),
    ELT(FLOOR(RAND() * 3) + 1, '대졸이상', '고졸이상', '무관'),
    DATE_ADD(CURDATE(), INTERVAL FLOOR(RAND() * 90) + 7 DAY),  -- 7~97일 후 마감
    'PUBLISHED',
    1,
    1,                                                      -- userNo=1 (테스트 기업 계정)
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY),     -- 최근 1년 내 랜덤 등록일
    NOW(),
    FLOOR(RAND() * 500),                                    -- 조회수 0~499
    IF(RAND() < 0.1, 1, 0)                                 -- 10% 광고 공고
FROM (
    -- 10만 행 생성용 크로스 조인
    SELECT (a.n + b.n * 10 + c.n * 100 + d.n * 1000 + e.n * 10000 + 1) AS seq
    FROM
        (SELECT 0 AS n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
         UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a
    CROSS JOIN
        (SELECT 0 AS n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
         UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) b
    CROSS JOIN
        (SELECT 0 AS n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
         UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) c
    CROSS JOIN
        (SELECT 0 AS n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
         UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) d
    CROSS JOIN
        (SELECT 0 AS n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
         UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) e
) seq_gen;

SELECT CONCAT('채용공고 삽입 완료: ', COUNT(*), '건') AS result FROM jobopening WHERE userNo = 1;


-- ── FULLTEXT 인덱스 생성 (없으면 생성, 있으면 무시) ──────────
-- 검색 성능 10배 향상. 삽입 후 한 번만 실행하면 됩니다.
SET @exists = (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'jobopening'
      AND INDEX_NAME   = 'ft_job_search'
);

SET @sql = IF(@exists = 0,
    'ALTER TABLE jobopening ADD FULLTEXT INDEX ft_job_search (title, content, company)',
    'SELECT "FULLTEXT INDEX ft_job_search 이미 존재함" AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- ── 지원 더미 데이터 5만 건 ──────────────────────────────────
-- 주의: apply 테이블에 uk_apply_user_job 유니크 제약이 있으므로
--       (user_no, job_no) 조합이 중복되지 않게 조인
-- userNo 2번을 지원자로 사용 (테스트 개인 계정)
-- 실제 존재하는 jobNo 범위에서 랜덤 선택
INSERT IGNORE INTO apply (resumeNo, jobNo, userNo, status, applyDate, message)
SELECT
    1,                                              -- resumeNo (테스트 이력서)
    j.jobNo,
    2,                                              -- userNo (테스트 개인 계정)
    ELT(FLOOR(RAND() * 4) + 1, 'APPLIED', 'REVIEWING', 'ACCEPTED', 'REJECTED'),
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 180) DAY),
    CONCAT('지원 메시지 ', j.jobNo)
FROM (
    SELECT jobNo FROM jobopening WHERE isActive = 1 AND status = 'PUBLISHED'
    ORDER BY RAND()
    LIMIT 50000
) j;

SELECT CONCAT('지원 데이터 삽입 완료: ', COUNT(*), '건') AS result FROM apply WHERE userNo = 2;
