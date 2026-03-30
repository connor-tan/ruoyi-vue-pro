-- 学校模块年级目录结构迁移
-- 1. 新增 edu_grade_catalog，维护阶段和年级标识的合法关系
-- 2. edu_school_grade 改为引用 grade_catalog_id
-- 3. edu_school_class 改为引用 school_grade_id

CREATE TABLE `edu_grade_catalog` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '年级目录ID',
  `stage` varchar(32) NOT NULL COMMENT '年级阶段',
  `grade_no` varchar(16) NOT NULL COMMENT '年级标识',
  `grade_name` varchar(32) NOT NULL COMMENT '年级名称',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_grade_catalog_stage_no` (`stage`, `grade_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='年级目录';

INSERT INTO `edu_grade_catalog`
(`stage`, `grade_no`, `grade_name`, `sort`, `status`, `creator`, `updater`, `deleted`)
VALUES
('kindergarten', 'K1', '小班', 10, 0, 'system', 'system', b'0'),
('kindergarten', 'K2', '中班', 20, 0, 'system', 'system', b'0'),
('kindergarten', 'K3', '大班', 30, 0, 'system', 'system', b'0'),
('primary', 'P1', '一年级', 40, 0, 'system', 'system', b'0'),
('primary', 'P2', '二年级', 50, 0, 'system', 'system', b'0'),
('primary', 'P3', '三年级', 60, 0, 'system', 'system', b'0'),
('primary', 'P4', '四年级', 70, 0, 'system', 'system', b'0'),
('primary', 'P5', '五年级', 80, 0, 'system', 'system', b'0'),
('primary', 'P6', '六年级', 90, 0, 'system', 'system', b'0'),
('middle', 'M1', '初一', 100, 0, 'system', 'system', b'0'),
('middle', 'M2', '初二', 110, 0, 'system', 'system', b'0'),
('middle', 'M3', '初三', 120, 0, 'system', 'system', b'0')
ON DUPLICATE KEY UPDATE
  `grade_name` = VALUES(`grade_name`),
  `sort` = VALUES(`sort`),
  `status` = VALUES(`status`),
  `updater` = 'system',
  `deleted` = b'0';

ALTER TABLE `edu_school_grade`
  ADD COLUMN `grade_catalog_id` bigint NULL COMMENT '年级目录ID' AFTER `school_id`;

UPDATE `edu_school_grade` sg
JOIN `edu_grade_catalog` gc
  ON gc.`stage` = sg.`stage`
 AND gc.`grade_no` = sg.`grade_no`
 AND gc.`deleted` = b'0'
SET sg.`grade_catalog_id` = gc.`id`;

ALTER TABLE `edu_school_class`
  ADD COLUMN `school_grade_id` bigint NULL COMMENT '学校年级ID' AFTER `entry_year`;

UPDATE `edu_school_class` sc
JOIN `edu_school_grade` sg
  ON sg.`school_id` = sc.`school_id`
 AND sg.`grade_no` = sc.`grade_no`
 AND sg.`deleted` = b'0'
SET sc.`school_grade_id` = sg.`id`;

ALTER TABLE `edu_school_class`
  MODIFY COLUMN `school_grade_id` bigint NOT NULL COMMENT '学校年级ID',
  DROP INDEX `system_school_class_pk_2`,
  DROP COLUMN `grade_no`,
  ADD UNIQUE KEY `uk_edu_school_class_school_grade`
    (`school_id`, `entry_year`, `school_year_id`, `school_grade_id`, `class_no`);

ALTER TABLE `edu_school_grade`
  MODIFY COLUMN `grade_catalog_id` bigint NOT NULL COMMENT '年级目录ID',
  DROP INDEX `edu_school_grade_pk`,
  DROP COLUMN `stage`,
  DROP COLUMN `grade_no`,
  ADD UNIQUE KEY `uk_edu_school_grade_school_catalog` (`school_id`, `grade_catalog_id`);
