SET NAMES utf8mb4;

SET @has_recommend_flag := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sub_window_publication'
    AND COLUMN_NAME = 'recommend_flag'
);
SET @add_recommend_flag_sql := IF(
  @has_recommend_flag = 0,
  'ALTER TABLE `sub_window_publication` ADD COLUMN `recommend_flag` bit(1) NOT NULL DEFAULT b''0'' COMMENT ''是否推荐'' AFTER `sort`',
  'SELECT 1'
);
PREPARE stmt FROM @add_recommend_flag_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_max_quantity := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sub_window_publication'
    AND COLUMN_NAME = 'max_quantity_per_student'
);
SET @add_max_quantity_sql := IF(
  @has_max_quantity = 0,
  'ALTER TABLE `sub_window_publication` ADD COLUMN `max_quantity_per_student` int NOT NULL DEFAULT ''1'' COMMENT ''每个学生最大订购数量'' AFTER `recommend_flag`',
  'SELECT 1'
);
PREPARE stmt FROM @add_max_quantity_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_publication_profile := (
  SELECT COUNT(*)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sub_publication_profile'
);

SET @backfill_recommend_flag_sql := IF(
  @has_publication_profile > 0,
  'UPDATE `sub_window_publication` wp
   INNER JOIN `sub_publication_profile` profile ON profile.`product_spu_id` = wp.`product_spu_id` AND profile.`deleted` = b''0''
   SET wp.`recommend_flag` = profile.`recommend_flag`
   WHERE wp.`deleted` = b''0''',
  'SELECT 1'
);
PREPARE stmt FROM @backfill_recommend_flag_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @backfill_max_quantity_sql := IF(
  @has_publication_profile > 0,
  'UPDATE `sub_window_publication` wp
   INNER JOIN `sub_publication_profile` profile ON profile.`product_spu_id` = wp.`product_spu_id` AND profile.`deleted` = b''0''
   SET wp.`max_quantity_per_student` = IFNULL(profile.`max_quantity_per_student`, 1)
   WHERE wp.`deleted` = b''0''',
  'SELECT 1'
);
PREPARE stmt FROM @backfill_max_quantity_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `sub_window_publication`
SET `max_quantity_per_student` = 1
WHERE `deleted` = b'0' AND (`max_quantity_per_student` IS NULL OR `max_quantity_per_student` < 1);

DROP TABLE IF EXISTS `sub_publication_profile`;
