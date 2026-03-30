-- 升班历史查询增强
-- 1. 补齐 edu_student_promotion_batch 的留级人数
-- 2. 补齐 edu_student_promotion_task 的留级人数

ALTER TABLE `edu_student_promotion_batch`
  ADD COLUMN `repeat_count` int NOT NULL DEFAULT 0 COMMENT '留级人数' AFTER `promoted_count`;

ALTER TABLE `edu_student_promotion_task`
  ADD COLUMN `repeat_count` int NOT NULL DEFAULT 0 COMMENT '留级人数' AFTER `promoted_count`;
