SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `sub_window_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `code` varchar(64) NOT NULL COMMENT '模板编码',
  `name` varchar(100) NOT NULL COMMENT '模板名称',
  `target_period` varchar(32) NOT NULL COMMENT '目标周期',
  `grade_calc_rule` varchar(32) NOT NULL COMMENT '年级判定规则',
  `description` varchar(500) DEFAULT NULL COMMENT '模板说明',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `built_in` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否内置模板',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sub_window_template_code` (`code`, `deleted`),
  UNIQUE KEY `uk_sub_window_template_name` (`name`, `deleted`)
) COMMENT='订刊窗口规则模板';

INSERT INTO `sub_window_template`
(`id`, `code`, `name`, `target_period`, `grade_calc_rule`, `description`, `status`, `sort`, `built_in`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(1, 'NEW_YEAR_PRE_SALE', '新学年预售', 'FULL_YEAR', 'PROMOTED_GRADE', '适用于新学年开学前预售，按升学后年级匹配刊物。', 0, 10, b'1', NULL, '', NOW(), '', NOW(), b'0'),
(2, 'BACK_TO_SCHOOL_RESTOCK', '开学补订', 'FULL_YEAR', 'CURRENT_GRADE', '适用于开学后的补订窗口，按当前学籍年级匹配刊物。', 0, 20, b'1', NULL, '', NOW(), '', NOW(), b'0'),
(3, 'SECOND_TERM_SUBSCRIPTION', '下学期订刊', 'SECOND_TERM', 'CURRENT_GRADE', '适用于下学期订刊窗口，按当前学籍年级匹配刊物。', 0, 30, b'1', NULL, '', NOW(), '', NOW(), b'0')
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`target_period` = VALUES(`target_period`),
`grade_calc_rule` = VALUES(`grade_calc_rule`),
`description` = VALUES(`description`),
`status` = VALUES(`status`),
`sort` = VALUES(`sort`),
`built_in` = VALUES(`built_in`),
`remark` = VALUES(`remark`),
`updater` = VALUES(`updater`),
`update_time` = VALUES(`update_time`),
`deleted` = VALUES(`deleted`);

ALTER TABLE `sub_window`
  ADD COLUMN `template_id` bigint DEFAULT NULL COMMENT '规则模板编号' AFTER `target_year_end`,
  ADD COLUMN `template_name_snapshot` varchar(100) DEFAULT NULL COMMENT '模板名称快照' AFTER `template_id`,
  ADD COLUMN `target_period` varchar(32) DEFAULT NULL COMMENT '目标周期' AFTER `template_name_snapshot`;

UPDATE `sub_window`
SET `target_period` = CASE `target_semester`
  WHEN 1 THEN 'FIRST_TERM'
  WHEN 2 THEN 'SECOND_TERM'
  ELSE 'FULL_YEAR'
END
WHERE `target_period` IS NULL;

UPDATE `sub_window`
SET `template_id` = CASE
  WHEN `target_period` = 'FULL_YEAR' AND `grade_calc_rule` = 'PROMOTED_GRADE' THEN 1
  WHEN `target_period` = 'FULL_YEAR' AND `grade_calc_rule` = 'CURRENT_GRADE' THEN 2
  WHEN `target_period` = 'SECOND_TERM' AND `grade_calc_rule` = 'CURRENT_GRADE' THEN 3
  ELSE NULL
END
WHERE `template_id` IS NULL;

UPDATE `sub_window` w
LEFT JOIN `sub_window_template` t ON t.id = w.template_id AND t.deleted = b'0'
SET w.template_name_snapshot = CASE
  WHEN t.id IS NOT NULL THEN t.name
  ELSE CONCAT(
    '历史迁移：',
    CASE w.target_period
      WHEN 'FIRST_TERM' THEN '上学期'
      WHEN 'SECOND_TERM' THEN '下学期'
      WHEN 'FULL_YEAR' THEN '全学年'
      ELSE '-'
    END,
    ' / ',
    CASE w.grade_calc_rule
      WHEN 'PROMOTED_GRADE' THEN '升学后年级'
      ELSE '当前学籍年级'
    END
  )
END
WHERE w.template_name_snapshot IS NULL OR w.template_name_snapshot = '';

ALTER TABLE `sub_window`
  MODIFY COLUMN `target_period` varchar(32) NOT NULL COMMENT '目标周期',
  MODIFY COLUMN `grade_calc_rule` varchar(32) NOT NULL COMMENT '年级判定规则',
  ADD KEY `idx_sub_window_template_id` (`template_id`);

ALTER TABLE `sub_window`
  DROP COLUMN `target_semester`;
