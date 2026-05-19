-- ============================================================
-- PeopleJob 부하테스트용 더미 데이터 삽입 스크립트
-- 대상 DB: peoplejob
-- 삽입 건수: 채용공고 10만 건
-- 실행: DBeaver에서 Ctrl+A → F5
-- ============================================================

USE peoplejob;

-- 기존 PUBLISHED 공고에서 company_no, userNo 자동 추출
SET @company_no  = (SELECT company_no FROM jobopening WHERE status = 'PUBLISHED' LIMIT 1);
SET @user_no_val = (SELECT userNo      FROM jobopening WHERE status = 'PUBLISHED' LIMIT 1);

SELECT CONCAT('사용할 company_no=', @company_no, ', userNo=', @user_no_val) AS info;

-- ── 채용공고 10만 건 ────────────────────────────────────────
INSERT INTO jobopening (
    company_no, userNo,
    title, content, company, location, jobType,
    salary, workType, career, education,
    deadline, status, isActive,
    regdate, updatedAt, viewCount, isAdvertised
)
SELECT
    @company_no,
    @user_no_val,
    CONCAT(
        ELT(FLOOR(RAND() * 10) + 1,
            'Java 백엔드', 'React 프론트엔드', 'Python 데이터', 'iOS 개발자', 'Android 개발자',
            '서버 DevOps', 'UI/UX 디자이너', '마케팅 담당자', '영업 관리자', '인사 담당자'
        ),
        ' 개발자 모집 #', seq
    ),
    CONCAT(
        '<p>저희 회사에서 ', seq, '번 포지션 인재를 모집합니다.</p>',
        '<p>업무: 서비스 개발 및 운영, 코드 리뷰, 기술 문서 작성</p>',
        '<p>우대사항: Spring Boot, React, Docker 경험자</p>'
    ),
    ELT(FLOOR(RAND() * 10) + 1,
       '모아모아', '서치온', '링크', '로켓대시', '딜리버리히어로즈', '핀플릭스', '이웃마켓', '스테이픽', '룸파인더', '새벽식탁'
    ),
    ELT(FLOOR(RAND() * 6) + 1, '서울', '부산', '대구', '인천', '광주', '대전'),
    ELT(FLOOR(RAND() * 6) + 1, 'IT개발', '영업', '마케팅', '디자인', '인사', '경영지원'),
    CONCAT((FLOOR(RAND() * 8) + 3) * 500, '만원'),
    ELT(FLOOR(RAND() * 3) + 1, '정규직', '계약직', '인턴'),
    ELT(FLOOR(RAND() * 3) + 1, '신입', '경력', '무관'),
    ELT(FLOOR(RAND() * 3) + 1, '대졸이상', '고졸이상', '무관'),
    DATE_ADD(CURDATE(), INTERVAL FLOOR(RAND() * 90) + 7 DAY),
    'PUBLISHED',
    1,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY),
    NOW(),
    FLOOR(RAND() * 500),
    IF(RAND() < 0.1, 1, 0)
FROM (
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

SELECT CONCAT('채용공고 삽입 완료: ', COUNT(*), '건 (PUBLISHED)') AS result
FROM jobopening WHERE status = 'PUBLISHED';


-- ── FULLTEXT 인덱스 (없으면 생성) ───────────────────────────
SET @ft_exists = (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'jobopening'
      AND INDEX_NAME   = 'ft_job_search'
);
SET @ft_sql = IF(@ft_exists = 0,
    'ALTER TABLE jobopening ADD FULLTEXT INDEX ft_job_search (title, content, company)',
    'SELECT "FULLTEXT INDEX 이미 존재함" AS info'
);
PREPARE stmt FROM @ft_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
