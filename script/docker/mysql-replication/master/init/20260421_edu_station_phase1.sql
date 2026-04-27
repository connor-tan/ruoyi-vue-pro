SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `edu_station` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '站点编号',
  `station_name` varchar(100) NOT NULL COMMENT '站点名称',
  `area_id` bigint NOT NULL COMMENT '区域编号',
  `contact_name` varchar(50) DEFAULT NULL COMMENT '联系人',
  `contact_mobile` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `station_address` varchar(255) NOT NULL COMMENT '站点地址',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='站点管理表';

ALTER TABLE `edu_station` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE `edu_station`
  MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '站点编号',
  MODIFY COLUMN `station_name` varchar(100) NOT NULL COMMENT '站点名称',
  MODIFY COLUMN `area_id` bigint NOT NULL COMMENT '区域编号';

SET @contact_name_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'edu_station'
    AND column_name = 'contact_name'
);
SET @contact_name_sql := IF(
  @contact_name_exists = 0,
  'ALTER TABLE `edu_station` ADD COLUMN `contact_name` varchar(50) DEFAULT NULL COMMENT ''联系人'' AFTER `area_id`',
  'SELECT 1'
);
PREPARE stmt FROM @contact_name_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @contact_mobile_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'edu_station'
    AND column_name = 'contact_mobile'
);
SET @contact_mobile_sql := IF(
  @contact_mobile_exists = 0,
  'ALTER TABLE `edu_station` ADD COLUMN `contact_mobile` varchar(20) DEFAULT NULL COMMENT ''联系电话'' AFTER `contact_name`',
  'SELECT 1'
);
PREPARE stmt FROM @contact_mobile_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @station_address_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'edu_station'
    AND column_name = 'station_address'
);
SET @station_address_sql := IF(
  @station_address_exists = 0,
  'ALTER TABLE `edu_station` ADD COLUMN `station_address` varchar(255) NOT NULL DEFAULT '''' COMMENT ''站点地址'' AFTER `contact_mobile`',
  'SELECT 1'
);
PREPARE stmt FROM @station_address_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sort_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'edu_station'
    AND column_name = 'sort'
);
SET @sort_sql := IF(
  @sort_exists = 0,
  'ALTER TABLE `edu_station` ADD COLUMN `sort` int NOT NULL DEFAULT 0 COMMENT ''排序'' AFTER `station_address`',
  'SELECT 1'
);
PREPARE stmt FROM @sort_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @status_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'edu_station'
    AND column_name = 'status'
);
SET @status_sql := IF(
  @status_exists = 0,
  'ALTER TABLE `edu_station` ADD COLUMN `status` tinyint NOT NULL DEFAULT 0 COMMENT ''状态'' AFTER `sort`',
  'SELECT 1'
);
PREPARE stmt FROM @status_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @remark_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'edu_station'
    AND column_name = 'remark'
);
SET @remark_sql := IF(
  @remark_exists = 0,
  'ALTER TABLE `edu_station` ADD COLUMN `remark` varchar(255) DEFAULT NULL COMMENT ''备注'' AFTER `status`',
  'SELECT 1'
);
PREPARE stmt FROM @remark_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @creator_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'edu_station'
    AND column_name = 'creator'
);
SET @creator_sql := IF(
  @creator_exists = 0,
  'ALTER TABLE `edu_station` ADD COLUMN `creator` varchar(64) DEFAULT '''' COMMENT ''创建者'' AFTER `remark`',
  'SELECT 1'
);
PREPARE stmt FROM @creator_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @create_time_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'edu_station'
    AND column_name = 'create_time'
);
SET @create_time_sql := IF(
  @create_time_exists = 0,
  'ALTER TABLE `edu_station` ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'' AFTER `creator`',
  'SELECT 1'
);
PREPARE stmt FROM @create_time_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @updater_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'edu_station'
    AND column_name = 'updater'
);
SET @updater_sql := IF(
  @updater_exists = 0,
  'ALTER TABLE `edu_station` ADD COLUMN `updater` varchar(64) DEFAULT '''' COMMENT ''更新者'' AFTER `create_time`',
  'SELECT 1'
);
PREPARE stmt FROM @updater_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @update_time_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'edu_station'
    AND column_name = 'update_time'
);
SET @update_time_sql := IF(
  @update_time_exists = 0,
  'ALTER TABLE `edu_station` ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'' AFTER `updater`',
  'SELECT 1'
);
PREPARE stmt FROM @update_time_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @deleted_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'edu_station'
    AND column_name = 'deleted'
);
SET @deleted_sql := IF(
  @deleted_exists = 0,
  'ALTER TABLE `edu_station` ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b''0'' COMMENT ''是否删除'' AFTER `update_time`',
  'SELECT 1'
);
PREPARE stmt FROM @deleted_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @station_name_idx_exists := (
  SELECT COUNT(1) FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'edu_station'
    AND index_name = 'uk_edu_station_area_name'
);
SET @station_name_idx_sql := IF(
  @station_name_idx_exists = 0,
  'ALTER TABLE `edu_station` ADD UNIQUE KEY `uk_edu_station_area_name` (`area_id`, `station_name`)',
  'SELECT 1'
);
PREPARE stmt FROM @station_name_idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @station_status_idx_exists := (
  SELECT COUNT(1) FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'edu_station'
    AND index_name = 'idx_edu_station_status_sort'
);
SET @station_status_idx_sql := IF(
  @station_status_idx_exists = 0,
  'ALTER TABLE `edu_station` ADD KEY `idx_edu_station_status_sort` (`status`, `sort`)',
  'SELECT 1'
);
PREPARE stmt FROM @station_status_idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @school_station_column_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'edu_school'
    AND column_name = 'station_id'
);
SET @school_station_column_sql := IF(
  @school_station_column_exists = 0,
  'ALTER TABLE `edu_school` ADD COLUMN `station_id` bigint DEFAULT NULL COMMENT ''归属站点编号'' AFTER `school_address`',
  'SELECT 1'
);
PREPARE stmt FROM @school_station_column_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @school_station_idx_exists := (
  SELECT COUNT(1) FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'edu_school'
    AND index_name = 'idx_edu_school_station_id'
);
SET @school_station_idx_sql := IF(
  @school_station_idx_exists = 0,
  'ALTER TABLE `edu_school` ADD KEY `idx_edu_school_station_id` (`station_id`)',
  'SELECT 1'
);
PREPARE stmt FROM @school_station_idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

DELETE FROM `system_role_menu`
WHERE `menu_id` IN (5620, 5621, 5622, 5623, 5624);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(5620, '站点管理', '', 2, 5, 5065, 'station', 'ep:van', 'edu/station/index', 'EduStation',
 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5621, '站点查询', 'edu:station:query', 3, 1, 5620, '', '', NULL, NULL,
 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5622, '站点新增', 'edu:station:create', 3, 2, 5620, '', '', NULL, NULL,
 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5623, '站点修改', 'edu:station:update', 3, 3, 5620, '', '', NULL, NULL,
 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5624, '站点删除', 'edu:station:delete', 3, 4, 5620, '', '', NULL, NULL,
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
(2, 5620, '1', NOW(), '1', NOW(), b'0', 1),
(2, 5621, '1', NOW(), '1', NOW(), b'0', 1),
(2, 5622, '1', NOW(), '1', NOW(), b'0', 1),
(2, 5623, '1', NOW(), '1', NOW(), b'0', 1),
(2, 5624, '1', NOW(), '1', NOW(), b'0', 1);
