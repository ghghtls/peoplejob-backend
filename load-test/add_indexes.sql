-- ============================================================
-- PeopleJob 인덱스 최적화 스크립트
-- DBeaver에서 Ctrl+A → Alt+X
-- ============================================================

USE peoplejob;

-- 1. 기존 idx_job_status_active 삭제 후 4컬럼 복합 인덱스 추가
SET @drop_old = (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'jobopening'
      AND INDEX_NAME   = 'idx_job_status_active'
);
SET @sql1 = IF(@drop_old > 0,
    'ALTER TABLE jobopening DROP INDEX idx_job_status_active',
    'SELECT "idx_job_status_active 없음 (건너뜀)" AS info'
);
PREPARE stmt FROM @sql1; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @add_list = (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'jobopening'
      AND INDEX_NAME   = 'idx_job_list'
);
SET @sql2 = IF(@add_list = 0,
    'ALTER TABLE jobopening ADD INDEX idx_job_list (status, isActive, isAdvertised, regdate)',
    'SELECT "idx_job_list 이미 존재" AS info'
);
PREPARE stmt FROM @sql2; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2. FULLTEXT 인덱스 (검색 성능 10배+)
SET @ft_exists = (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'jobopening'
      AND INDEX_NAME   = 'ft_job_search'
);
SET @sql3 = IF(@ft_exists = 0,
    'ALTER TABLE jobopening ADD FULLTEXT INDEX ft_job_search (title, content, company)',
    'SELECT "ft_job_search 이미 존재" AS info'
);
PREPARE stmt FROM @sql3; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3. 카테고리 필터용 인덱스
SET @cat_exists = (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'jobopening'
      AND INDEX_NAME   = 'idx_job_category'
);
SET @sql4 = IF(@cat_exists = 0,
    'ALTER TABLE jobopening ADD INDEX idx_job_category (status, isActive, jobType, location)',
    'SELECT "idx_job_category 이미 존재" AS info'
);
PREPARE stmt FROM @sql4; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 결과 확인
SELECT INDEX_NAME, GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS columns, INDEX_TYPE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'jobopening'
GROUP BY INDEX_NAME, INDEX_TYPE
ORDER BY INDEX_NAME;
