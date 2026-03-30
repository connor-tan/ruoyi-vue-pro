-- 学生一键升班能力
-- 1. 增强 edu_student_flow，增加批次和备注
-- 2. 新增 edu_student_promotion_batch，记录批量升班执行结果

ALTER TABLE `edu_student_flow`
  ADD COLUMN `batch_id` bigint DEFAULT NULL COMMENT '升班批次ID' AFTER `student_id`,
  ADD COLUMN `remark` varchar(255) DEFAULT NULL COMMENT '备注' AFTER `effective_date`,
  ADD COLUMN `status` tinyint NOT NULL DEFAULT 1 COMMENT '流转状态' AFTER `effective_date`,
  ADD COLUMN `target_class_created` bit(1) NOT NULL DEFAULT b'0' COMMENT '目标班级是否由本次任务自动创建' AFTER `remark`,
  ADD KEY `idx_edu_student_flow_batch_id` (`batch_id`),
  ADD KEY `idx_edu_student_flow_student_effective_date` (`student_id`, `effective_date`);

CREATE TABLE `edu_student_promotion_batch` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `school_id` bigint NOT NULL COMMENT '学校ID',
  `from_school_year_id` bigint DEFAULT NULL COMMENT '来源学年ID',
  `to_school_year_id` bigint DEFAULT NULL COMMENT '目标学年ID',
  `auto_create_class` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否自动创建目标班级',
  `graduate_terminal_student` bit(1) NOT NULL DEFAULT b'1' COMMENT '末级学生是否自动毕业',
  `total_count` int NOT NULL DEFAULT 0 COMMENT '总人数',
  `promoted_count` int NOT NULL DEFAULT 0 COMMENT '升班人数',
  `repeat_count` int NOT NULL DEFAULT 0 COMMENT '留级人数',
  `graduated_count` int NOT NULL DEFAULT 0 COMMENT '毕业人数',
  `skipped_count` int NOT NULL DEFAULT 0 COMMENT '跳过人数',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  `reason` varchar(255) DEFAULT NULL COMMENT '状态原因',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_edu_student_promotion_batch_school_year` (`school_id`, `from_school_year_id`, `to_school_year_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生一键升班批次';
