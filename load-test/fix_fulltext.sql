-- ============================================================
-- FULLTEXT 인덱스 재생성 (한국어 + 영어 지원)
-- DBeaver에서 Ctrl+A → Alt+X
-- ============================================================

USE peoplejob;

-- 기존 인덱스 삭제 (있으면)
SET @drop_ft = (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'jobopening'
      AND INDEX_NAME   = 'ft_job_search'
);
SET @sql1 = IF(@drop_ft > 0,
    'ALTER TABLE jobopening DROP INDEX ft_job_search',
    'SELECT "ft_job_search 없음 (건너뜀)" AS info'
);
PREPARE stmt FROM @sql1; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ngram 파서로 재생성 (한국어 + 짧은 영어 단어 지원)
ALTER TABLE jobopening
    ADD FULLTEXT INDEX ft_job_search (title, content, company)
    WITH PARSER ngram;

-- 확인
SELECT INDEX_NAME, INDEX_TYPE, COMMENT
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME   = 'jobopening'
  AND INDEX_NAME   = 'ft_job_search'
LIMIT 1;

SELECT 'FULLTEXT ngram 인덱스 생성 완료' AS result;
