-- 升班审计增强
-- 1. 为 edu_student_promotion_batch 增加状态原因
-- 2. 为 edu_student_flow 增加流转状态
-- 3. 修正历史上回滚批次的状态编码

ALTER TABLE `edu_student_promotion_batch`
  ADD COLUMN IF NOT EXISTS `reason` varchar(255) DEFAULT NULL COMMENT '状态原因' AFTER `status`;

ALTER TABLE `edu_student_flow`
  ADD COLUMN IF NOT EXISTS `status` tinyint NOT NULL DEFAULT 1 COMMENT '流转状态' AFTER `effective_date`;

UPDATE `edu_student_promotion_batch` b
INNER JOIN `edu_student_promotion_task` t ON t.id = b.task_id
SET b.`status` = 4
WHERE b.`status` = 2
  AND t.`status` = 4;

ALTER TABLE `edu_student_promotion_batch`
  MODIFY COLUMN `from_school_year_id` bigint DEFAULT NULL COMMENT '来源学年ID',
  MODIFY COLUMN `to_school_year_id` bigint DEFAULT NULL COMMENT '目标学年ID';
