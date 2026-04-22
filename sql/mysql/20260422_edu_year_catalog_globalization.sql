SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `edu_year_catalog` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '学年目录编号',
  `year_start` int NOT NULL COMMENT '学年开始年份',
  `year_end` int NOT NULL COMMENT '学年结束年份',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_year_catalog_year_range` (`year_start`, `year_end`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='全局学年目录';

ALTER TABLE `edu_year_catalog`
  CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

SET @school_year_year_catalog_column_exists := (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'edu_school_year'
    AND column_name = 'year_catalog_id'
);
SET @school_year_year_catalog_column_sql := IF(
  @school_year_year_catalog_column_exists = 0,
  'ALTER TABLE `edu_school_year` ADD COLUMN `year_catalog_id` bigint DEFAULT NULL COMMENT ''全局学年目录编号'' AFTER `school_id`',
  'SELECT 1'
);
PREPARE stmt FROM @school_year_year_catalog_column_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sub_window_year_catalog_column_exists := (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'sub_window'
    AND column_name = 'target_year_catalog_id'
);
SET @sub_window_year_catalog_column_sql := IF(
  @sub_window_year_catalog_column_exists = 0,
  'ALTER TABLE `sub_window` ADD COLUMN `target_year_catalog_id` bigint DEFAULT NULL COMMENT ''目标学年目录编号'' AFTER `target_year_end`',
  'SELECT 1'
);
PREPARE stmt FROM @sub_window_year_catalog_column_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT INTO `edu_year_catalog`
(`year_start`, `year_end`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT DISTINCT src.year_start, src.year_end, 'system', NOW(), 'system', NOW(), b'0'
FROM (
  SELECT `year_start`, `year_end`
  FROM `edu_school_year`
  WHERE `year_start` IS NOT NULL
    AND `year_end` IS NOT NULL
  UNION
  SELECT `target_year_start` AS `year_start`, `target_year_end` AS `year_end`
  FROM `sub_window`
  WHERE `target_year_start` IS NOT NULL
    AND `target_year_end` IS NOT NULL
) src
WHERE src.year_start IS NOT NULL
  AND src.year_end IS NOT NULL
ON DUPLICATE KEY UPDATE
`updater` = VALUES(`updater`),
`update_time` = VALUES(`update_time`),
`deleted` = VALUES(`deleted`);

UPDATE `edu_school_year` sy
JOIN `edu_year_catalog` yc
  ON yc.`year_start` = sy.`year_start`
 AND yc.`year_end` = sy.`year_end`
 AND yc.`deleted` = b'0'
SET sy.`year_catalog_id` = yc.`id`
WHERE sy.`year_catalog_id` IS NULL;

UPDATE `sub_window` w
JOIN `edu_year_catalog` yc
  ON yc.`year_start` = w.`target_year_start`
 AND yc.`year_end` = w.`target_year_end`
 AND yc.`deleted` = b'0'
SET w.`target_year_catalog_id` = yc.`id`
WHERE w.`target_year_catalog_id` IS NULL;

SET @school_year_catalog_idx_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'edu_school_year'
    AND index_name = 'idx_edu_school_year_year_catalog_id'
);
SET @school_year_catalog_idx_sql := IF(
  @school_year_catalog_idx_exists = 0,
  'ALTER TABLE `edu_school_year` ADD KEY `idx_edu_school_year_year_catalog_id` (`year_catalog_id`)',
  'SELECT 1'
);
PREPARE stmt FROM @school_year_catalog_idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @school_year_unique_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'edu_school_year'
    AND index_name = 'uk_edu_school_year_school_catalog'
);
SET @school_year_unique_sql := IF(
  @school_year_unique_exists = 0,
  'ALTER TABLE `edu_school_year` ADD UNIQUE KEY `uk_edu_school_year_school_catalog` (`school_id`, `year_catalog_id`)',
  'SELECT 1'
);
PREPARE stmt FROM @school_year_unique_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sub_window_catalog_idx_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'sub_window'
    AND index_name = 'idx_sub_window_target_year_catalog_id'
);
SET @sub_window_catalog_idx_sql := IF(
  @sub_window_catalog_idx_exists = 0,
  'ALTER TABLE `sub_window` ADD KEY `idx_sub_window_target_year_catalog_id` (`target_year_catalog_id`)',
  'SELECT 1'
);
PREPARE stmt FROM @sub_window_catalog_idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @school_year_year_catalog_null_count := (
  SELECT COUNT(1)
  FROM `edu_school_year`
  WHERE `year_catalog_id` IS NULL
);
SET @school_year_year_catalog_not_null_sql := IF(
  @school_year_year_catalog_null_count = 0,
  'ALTER TABLE `edu_school_year` MODIFY COLUMN `year_catalog_id` bigint NOT NULL COMMENT ''全局学年目录编号''',
  'SELECT 1'
);
PREPARE stmt FROM @school_year_year_catalog_not_null_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sub_window_year_catalog_null_count := (
  SELECT COUNT(1)
  FROM `sub_window`
  WHERE `target_year_catalog_id` IS NULL
);
SET @sub_window_year_catalog_not_null_sql := IF(
  @sub_window_year_catalog_null_count = 0,
  'ALTER TABLE `sub_window` MODIFY COLUMN `target_year_catalog_id` bigint NOT NULL COMMENT ''目标学年目录编号''',
  'SELECT 1'
);
PREPARE stmt FROM @sub_window_year_catalog_not_null_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

DELETE FROM `system_role_menu`
WHERE `menu_id` IN (5625, 5626, 5627, 5628, 5629);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(5625, '学年目录', '', 2, 6, 5065, 'year-catalog', 'ep:calendar', 'edu/year-catalog/index', 'EduYearCatalog',
 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5626, '学年目录查询', 'edu:year-catalog:query', 3, 1, 5625, '', '', NULL, NULL,
 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5627, '学年目录新增', 'edu:year-catalog:create', 3, 2, 5625, '', '', NULL, NULL,
 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5628, '学年目录修改', 'edu:year-catalog:update', 3, 3, 5625, '', '', NULL, NULL,
 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5629, '学年目录删除', 'edu:year-catalog:delete', 3, 4, 5625, '', '', NULL, NULL,
 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0')
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`permission` = VALUES(`permission`),
`type` = VALUES(`type`),
`sort` = VALUES(`sort`),
`parent_id` = VALUES(`parent_id`),
`path` = VALUES(`path`),
`icon` = VALUES(`icon`),
`component` = VALUES(`component`),
`component_name` = VALUES(`component_name`),
`status` = VALUES(`status`),
`visible` = VALUES(`visible`),
`keep_alive` = VALUES(`keep_alive`),
`always_show` = VALUES(`always_show`),
`updater` = VALUES(`updater`),
`update_time` = VALUES(`update_time`),
`deleted` = VALUES(`deleted`);

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(2, 5625, '1', NOW(), '1', NOW(), b'0', 1),
(2, 5626, '1', NOW(), '1', NOW(), b'0', 1),
(2, 5627, '1', NOW(), '1', NOW(), b'0', 1),
(2, 5628, '1', NOW(), '1', NOW(), b'0', 1),
(2, 5629, '1', NOW(), '1', NOW(), b'0', 1);
