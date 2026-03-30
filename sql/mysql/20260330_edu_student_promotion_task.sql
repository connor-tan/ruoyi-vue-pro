-- 学生全局批量升班能力
-- 1. 新增 edu_student_promotion_task，记录全局升班任务
-- 2. 增强 edu_student_promotion_batch，关联全局任务

ALTER TABLE `edu_student_promotion_batch`
  ADD COLUMN `task_id` bigint DEFAULT NULL COMMENT '全局升班任务ID' AFTER `id`,
  ADD KEY `idx_edu_student_promotion_batch_task_id` (`task_id`);

CREATE TABLE `edu_student_promotion_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `from_year_start` int NOT NULL COMMENT '来源学年开始年份',
  `to_year_start` int NOT NULL COMMENT '目标学年开始年份',
  `scope_type` varchar(16) NOT NULL COMMENT '范围类型',
  `scope_snapshot` text COMMENT '范围快照',
  `auto_create_class` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否自动创建目标班级',
  `graduate_terminal_student` bit(1) NOT NULL DEFAULT b'1' COMMENT '末级学生是否自动毕业',
  `total_school_count` int NOT NULL DEFAULT 0 COMMENT '总学校数',
  `success_school_count` int NOT NULL DEFAULT 0 COMMENT '执行成功学校数',
  `skipped_school_count` int NOT NULL DEFAULT 0 COMMENT '跳过学校数',
  `failed_school_count` int NOT NULL DEFAULT 0 COMMENT '执行失败学校数',
  `total_count` int NOT NULL DEFAULT 0 COMMENT '总人数',
  `promoted_count` int NOT NULL DEFAULT 0 COMMENT '升班人数',
  `repeat_count` int NOT NULL DEFAULT 0 COMMENT '留级人数',
  `graduated_count` int NOT NULL DEFAULT 0 COMMENT '毕业人数',
  `skipped_count` int NOT NULL DEFAULT 0 COMMENT '跳过人数',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '任务状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_edu_student_promotion_task_year` (`from_year_start`, `to_year_start`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生全局升班任务';
