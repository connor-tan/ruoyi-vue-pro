-- 校刊汇：学校年级班级容量与 APP 按需建班支持

SET NAMES utf8mb4;

SET @column_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'edu_school_grade'
      AND COLUMN_NAME = 'max_class_no'
);
SET @sql := IF(@column_exists = 0,
    'ALTER TABLE edu_school_grade ADD COLUMN max_class_no INT NOT NULL DEFAULT 0 COMMENT ''最大班号/班级容量，0 表示暂不开放 APP 选择或自动建班'' AFTER grade_catalog_id',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE edu_school_grade
    MODIFY COLUMN max_class_no INT NOT NULL DEFAULT 0 COMMENT '最大班号/班级容量，0 表示暂不开放 APP 选择或自动建班';

UPDATE edu_school_grade sg
JOIN (
    SELECT school_grade_id, MAX(class_no) AS max_class_no
    FROM edu_school_class
    WHERE deleted = b'0'
    GROUP BY school_grade_id
) c ON c.school_grade_id = sg.id
SET sg.max_class_no = GREATEST(COALESCE(sg.max_class_no, 0), c.max_class_no),
    sg.update_time = NOW()
WHERE sg.deleted = b'0';

UPDATE edu_school_grade sg
JOIN edu_grade_catalog gc ON gc.id = sg.grade_catalog_id
    AND gc.deleted = b'0'
SET sg.max_class_no = CASE gc.stage
        WHEN 'kindergarten' THEN 10
        WHEN 'primary' THEN 25
        WHEN 'middle' THEN 10
        ELSE sg.max_class_no
    END,
    sg.update_time = NOW()
WHERE sg.deleted = b'0'
  AND sg.max_class_no = 0
  AND gc.stage IN ('kindergarten', 'primary', 'middle');

SET @index_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'edu_school_class'
      AND INDEX_NAME = 'uk_edu_school_class_active_unique'
);
SET @sql := IF(@index_exists = 0,
    'ALTER TABLE edu_school_class ADD UNIQUE KEY uk_edu_school_class_active_unique (school_id, entry_year, school_year_id, school_grade_id, class_no, deleted)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

