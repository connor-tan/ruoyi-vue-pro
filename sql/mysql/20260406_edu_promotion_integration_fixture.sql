SET NAMES utf8mb4;

-- 说明：
-- 1. 该脚本用于本地升班联调造数，复用“海棠实验小学（升班测试）”
-- 2. 场景覆盖：
--    - 三名五年级在读学生：用于正常升班 / 人工调班
--    - 一名六年级在读学生：用于末级转待升学
-- 3. 学生编码固定在 910101 ~ 910104，重复执行会先清理旧样本

SET @school_name := _utf8mb4'海棠实验小学（升班测试）' COLLATE utf8mb4_unicode_ci;
SET @from_year_start := 2025;
SET @to_year_start := 2026;

SET @school_id := (
    SELECT id FROM edu_school
    WHERE school_name = @school_name AND deleted = b'0'
    LIMIT 1
);

SET @from_school_year_id := (
    SELECT id FROM edu_school_year
    WHERE school_id = @school_id AND year_start = @from_year_start AND deleted = b'0'
    LIMIT 1
);

SET @to_school_year_id := (
    SELECT id FROM edu_school_year
    WHERE school_id = @school_id AND year_start = @to_year_start AND deleted = b'0'
    LIMIT 1
);

SET @from_p5_class_id := (
    SELECT id FROM edu_school_class
    WHERE school_id = @school_id
      AND school_year_id = @from_school_year_id
      AND class_name = _utf8mb4'2021级五年级1班' COLLATE utf8mb4_unicode_ci
      AND deleted = b'0'
    LIMIT 1
);

SET @from_p6_class_id := (
    SELECT id FROM edu_school_class
    WHERE school_id = @school_id
      AND school_year_id = @from_school_year_id
      AND class_name = _utf8mb4'2020级六年级1班' COLLATE utf8mb4_unicode_ci
      AND deleted = b'0'
    LIMIT 1
);

SET @to_p6_class1_id := (
    SELECT id FROM edu_school_class
    WHERE school_id = @school_id
      AND school_year_id = @to_school_year_id
      AND class_name = _utf8mb4'2021级六年级1班' COLLATE utf8mb4_unicode_ci
      AND deleted = b'0'
    LIMIT 1
);

SET @to_p6_class2_id := (
    SELECT id FROM edu_school_class
    WHERE school_id = @school_id
      AND school_year_id = @to_school_year_id
      AND class_name = _utf8mb4'2021级六年级2班' COLLATE utf8mb4_unicode_ci
      AND deleted = b'0'
    LIMIT 1
);

-- 清理同批旧样本
DELETE sc
FROM edu_student_class sc
INNER JOIN edu_student s ON s.id = sc.student_id
WHERE s.current_school_id = @school_id
  AND s.student_code IN (910101, 910102, 910103, 910104);

DELETE FROM edu_student
WHERE current_school_id = @school_id
  AND student_code IN (910101, 910102, 910103, 910104);

INSERT INTO edu_student (
    student_name, belong_to, current_school_id, entry_year, student_code, status, creator, updater, deleted
) VALUES
    ('林书言', 1, @school_id, 2021, 910101, 1, 'codex', 'codex', b'0'),
    ('钱知远', 1, @school_id, 2021, 910102, 1, 'codex', 'codex', b'0'),
    ('周沐阳', 1, @school_id, 2021, 910103, 1, 'codex', 'codex', b'0'),
    ('许一帆', 1, @school_id, 2020, 910104, 1, 'codex', 'codex', b'0');

INSERT INTO edu_student_class (
    student_id, class_id, start_date, end_date, creator, updater, deleted
)
SELECT s.id, @from_p5_class_id, '2025-09-01', NULL, 'codex', 'codex', b'0'
FROM edu_student s
WHERE s.current_school_id = @school_id
  AND s.student_code IN (910101, 910102, 910103);

INSERT INTO edu_student_class (
    student_id, class_id, start_date, end_date, creator, updater, deleted
)
SELECT s.id, @from_p6_class_id, '2025-09-01', NULL, 'codex', 'codex', b'0'
FROM edu_student s
WHERE s.current_school_id = @school_id
  AND s.student_code = 910104;

SELECT
    @school_id AS school_id,
    @from_school_year_id AS from_school_year_id,
    @to_school_year_id AS to_school_year_id,
    @from_p5_class_id AS from_p5_class_id,
    @from_p6_class_id AS from_p6_class_id,
    @to_p6_class1_id AS to_p6_class1_id,
    @to_p6_class2_id AS to_p6_class2_id;

SELECT id, student_name, student_code, entry_year, status
FROM edu_student
WHERE current_school_id = @school_id
  AND student_code IN (910101, 910102, 910103, 910104)
ORDER BY student_code;
