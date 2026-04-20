-- 学校办学学段：用于约束学校年级定义只能选择学校实际开设的学段

CREATE TABLE IF NOT EXISTS `edu_school_stage` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录编号',
  `school_id` bigint NOT NULL COMMENT '学校编号',
  `stage` varchar(32) NOT NULL COMMENT '办学学段，对齐 edu_grade_catalog.stage / edu_stage 字典',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_school_stage_school_stage` (`school_id`, `stage`),
  KEY `idx_edu_school_stage_stage` (`stage`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学校办学学段';

ALTER TABLE `edu_school_stage` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

INSERT INTO `edu_school_stage` (`school_id`, `stage`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT source_data.`school_id`, source_data.`stage`, '', NOW(), '', NOW(), b'0'
FROM (
  SELECT DISTINCT sg.`school_id`, gc.`stage`
  FROM `edu_school_grade` sg
  JOIN `edu_grade_catalog` gc ON gc.`id` = sg.`grade_catalog_id`
  WHERE sg.`deleted` = b'0'
    AND gc.`deleted` = b'0'
    AND gc.`status` = 0
    AND gc.`stage` IS NOT NULL
    AND gc.`stage` <> ''
) source_data
WHERE NOT EXISTS (
  SELECT 1
  FROM `edu_school_stage` existed
  WHERE existed.`school_id` = source_data.`school_id`
    AND existed.`stage` = source_data.`stage`
);
