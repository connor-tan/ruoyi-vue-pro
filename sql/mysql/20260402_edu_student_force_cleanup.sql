SET NAMES utf8mb4;

-- 开发/维护模式：按学生强制清理升班痕迹
-- 使用方法：
-- 1. 修改下方 @student_id
-- 2. 在确认环境后执行整段脚本
-- 注意：
-- - 这是物理删除
-- - 会破坏该学生参与过的升班历史审计链
-- - 会重算相关批次/任务汇总，并删除空批次、空任务

SET @student_id := 0;

START TRANSACTION;

CREATE TEMPORARY TABLE tmp_student_batch_ids AS
SELECT DISTINCT batch_id
FROM edu_student_flow
WHERE student_id = @student_id
  AND batch_id IS NOT NULL;

CREATE TEMPORARY TABLE tmp_student_task_ids AS
SELECT DISTINCT task_id
FROM edu_student_promotion_batch
WHERE id IN (SELECT batch_id FROM tmp_student_batch_ids)
  AND task_id IS NOT NULL;

DELETE FROM edu_student_flow
WHERE student_id = @student_id;

DELETE FROM edu_student_class
WHERE student_id = @student_id;

DELETE FROM edu_student
WHERE id = @student_id;

UPDATE edu_student_promotion_batch b
LEFT JOIN (
    SELECT
        f.batch_id,
        COUNT(*) AS total_count,
        SUM(CASE WHEN f.change_type = 'PROMOTE' THEN 1 ELSE 0 END) AS promoted_count,
        SUM(CASE WHEN f.change_type = 'REPEAT' THEN 1 ELSE 0 END) AS repeat_count,
        SUM(CASE WHEN f.change_type = 'PENDING_ADVANCE' THEN 1 ELSE 0 END) AS graduated_count
    FROM edu_student_flow f
    WHERE f.batch_id IN (SELECT batch_id FROM tmp_student_batch_ids)
    GROUP BY f.batch_id
) x ON b.id = x.batch_id
SET
    b.total_count = IFNULL(x.total_count, 0),
    b.promoted_count = IFNULL(x.promoted_count, 0),
    b.repeat_count = IFNULL(x.repeat_count, 0),
    b.graduated_count = IFNULL(x.graduated_count, 0)
WHERE b.id IN (SELECT batch_id FROM tmp_student_batch_ids);

DELETE b
FROM edu_student_promotion_batch b
LEFT JOIN edu_student_flow f ON f.batch_id = b.id
WHERE b.id IN (SELECT batch_id FROM tmp_student_batch_ids)
GROUP BY b.id
HAVING COUNT(f.id) = 0;

UPDATE edu_student_promotion_task t
LEFT JOIN (
    SELECT
        b.task_id,
        COUNT(DISTINCT CASE WHEN b.status = 1 THEN b.id END) AS success_school_count,
        COUNT(DISTINCT CASE WHEN b.status = 3 THEN b.id END) AS skipped_school_count,
        COUNT(DISTINCT CASE WHEN b.status = 2 THEN b.id END) AS failed_school_count,
        SUM(IFNULL(b.total_count, 0)) AS total_count,
        SUM(IFNULL(b.promoted_count, 0)) AS promoted_count,
        SUM(IFNULL(b.repeat_count, 0)) AS repeat_count,
        SUM(IFNULL(b.graduated_count, 0)) AS graduated_count
    FROM edu_student_promotion_batch b
    WHERE b.task_id IN (SELECT task_id FROM tmp_student_task_ids)
    GROUP BY b.task_id
) x ON t.id = x.task_id
SET
    t.success_school_count = IFNULL(x.success_school_count, 0),
    t.skipped_school_count = IFNULL(x.skipped_school_count, 0),
    t.failed_school_count = IFNULL(x.failed_school_count, 0),
    t.total_count = IFNULL(x.total_count, 0),
    t.promoted_count = IFNULL(x.promoted_count, 0),
    t.repeat_count = IFNULL(x.repeat_count, 0),
    t.graduated_count = IFNULL(x.graduated_count, 0)
WHERE t.id IN (SELECT task_id FROM tmp_student_task_ids);

DELETE t
FROM edu_student_promotion_task t
LEFT JOIN edu_student_promotion_batch b ON b.task_id = t.id
WHERE t.id IN (SELECT task_id FROM tmp_student_task_ids)
GROUP BY t.id
HAVING COUNT(b.id) = 0;

DROP TEMPORARY TABLE IF EXISTS tmp_student_task_ids;
DROP TEMPORARY TABLE IF EXISTS tmp_student_batch_ids;

COMMIT;
