SET NAMES utf8mb4;

-- 开发/维护模式：全量清理学生与升班痕迹
-- 注意：
-- - 这是物理删除
-- - 会删除所有学生、学生班级区间、学生流转、升班批次、升班任务
-- - 会尝试删除由升班自动创建且当前已无学生引用的班级

START TRANSACTION;

CREATE TEMPORARY TABLE tmp_auto_created_class_ids AS
SELECT DISTINCT to_class_id AS class_id
FROM edu_student_flow
WHERE target_class_created = b'1'
  AND to_class_id IS NOT NULL;

DELETE FROM edu_student_flow;

DELETE FROM edu_student_class;

DELETE FROM edu_student;

DELETE FROM edu_student_promotion_batch;

DELETE FROM edu_student_promotion_task;

DELETE sc
FROM edu_school_class sc
LEFT JOIN edu_student_class stc ON stc.class_id = sc.id
WHERE sc.id IN (SELECT class_id FROM tmp_auto_created_class_ids)
  AND stc.id IS NULL;

DROP TEMPORARY TABLE IF EXISTS tmp_auto_created_class_ids;

COMMIT;
