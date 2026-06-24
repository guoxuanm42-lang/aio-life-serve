-- 2026-06-24 database change
-- CreatedAt: 2026-06-24
-- Author: Ethan
-- Summary:
-- 1) Add pseudo_code to problem_note for optional pseudocode notes.
SET NAMES utf8mb4;

SET @problem_note_pseudo_code_column_exists = (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'problem_note'
      AND COLUMN_NAME = 'pseudo_code'
);

SET @problem_note_pseudo_code_column_sql = IF(
    @problem_note_pseudo_code_column_exists = 0,
    'ALTER TABLE `problem_note` ADD COLUMN `pseudo_code` MEDIUMTEXT DEFAULT NULL COMMENT ''Pseudocode'' AFTER `solution_code`',
    'SELECT 1'
);

PREPARE stmt FROM @problem_note_pseudo_code_column_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
