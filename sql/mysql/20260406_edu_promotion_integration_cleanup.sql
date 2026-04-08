SET NAMES utf8mb4;

-- 说明：
-- 1. 清理 2026-04-06 Codex 升班联调所造的学生、流转、批次和任务残留
-- 2. 执行前请确认不再需要保留这批联调历史

SET @school_name := '海棠实验小学（升班测试）';
SET @school_id := (
    SELECT id FROM edu_school
    WHERE school_name = @school_name AND deleted = b'0'
    LIMIT 1
);

CREATE TEMPORARY TABLE tmp_codex_student_ids AS
SELECT id
FROM edu_student
WHERE current_school_id = @school_id
  AND student_code IN (910101, 910102, 910103, 910104);

CREATE TEMPORARY TABLE tmp_codex_batch_ids AS
SELECT DISTINCT batch_id
FROM edu_student_flow
WHERE student_id IN (SELECT id FROM tmp_codex_student_ids)
  AND batch_id IS NOT NULL;

CREATE TEMPORARY TABLE tmp_codex_task_ids AS
SELECT DISTINCT task_id
FROM edu_student_promotion_batch
WHERE id IN (SELECT batch_id FROM tmp_codex_batch_ids)
  AND task_id IS NOT NULL;

DELETE FROM edu_student_flow
WHERE student_id IN (SELECT id FROM tmp_codex_student_ids);

DELETE FROM edu_student_promotion_batch
WHERE id IN (SELECT batch_id FROM tmp_codex_batch_ids)
   OR (school_id = @school_id AND remark LIKE 'Codex升班联调-20260406%');

DELETE FROM edu_student_promotion_task
WHERE id IN (SELECT task_id FROM tmp_codex_task_ids)
   OR remark LIKE 'Codex升班联调-20260406%';

DELETE FROM edu_student_class
WHERE student_id IN (SELECT id FROM tmp_codex_student_ids);

DELETE FROM edu_student
WHERE id IN (SELECT id FROM tmp_codex_student_ids);

DROP TEMPORARY TABLE IF EXISTS tmp_codex_task_ids;
DROP TEMPORARY TABLE IF EXISTS tmp_codex_batch_ids;
DROP TEMPORARY TABLE IF EXISTS tmp_codex_student_ids;
