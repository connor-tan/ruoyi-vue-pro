SET NAMES utf8mb4;

SET @has_product_category_supports_gift := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'product_category'
    AND COLUMN_NAME = 'supports_gift'
);
SET @add_product_category_supports_gift_sql := IF(
  @has_product_category_supports_gift = 0,
  'ALTER TABLE `product_category` ADD COLUMN `supports_gift` bit(1) NOT NULL DEFAULT b''0'' COMMENT ''是否参与赠送'' AFTER `status`',
  'SELECT 1'
);
PREPARE stmt FROM @add_product_category_supports_gift_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT INTO `product_category`
(`id`, `parent_id`, `name`, `pic_url`, `sort`, `status`, `supports_gift`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(90001, 0, '订刊类型', '', 90001, 0, b'0', '', NOW(), '', NOW(), b'0', 0),
(90002, 90001, '报纸', '', 1, 0, b'1', '', NOW(), '', NOW(), b'0', 0),
(90003, 90001, '杂志', '', 2, 0, b'0', '', NOW(), '', NOW(), b'0', 0),
(90004, 90001, '教辅', '', 3, 0, b'0', '', NOW(), '', NOW(), b'0', 0)
ON DUPLICATE KEY UPDATE
`parent_id` = VALUES(`parent_id`),
`name` = VALUES(`name`),
`pic_url` = VALUES(`pic_url`),
`sort` = VALUES(`sort`),
`status` = VALUES(`status`),
`supports_gift` = VALUES(`supports_gift`),
`updater` = VALUES(`updater`),
`update_time` = VALUES(`update_time`),
`deleted` = VALUES(`deleted`),
`tenant_id` = VALUES(`tenant_id`);

DELETE FROM `infra_config` WHERE `config_key` = 'subscription.publication_type_root_category_id';
INSERT INTO `infra_config`
(`id`, `category`, `type`, `name`, `config_key`, `value`, `visible`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(14, 'subscription', 2, '订刊类型根分类', 'subscription.publication_type_root_category_id', '90001', b'0', '订刊模块刊物类型根分类', '', NOW(), '', NOW(), b'0');

SET @has_publication_type := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sub_publication_profile'
    AND COLUMN_NAME = 'publication_type'
);
SET @update_product_spu_category_sql := IF(
  @has_publication_type > 0,
  'UPDATE `product_spu` spu
   INNER JOIN `sub_publication_profile` profile ON profile.`product_spu_id` = spu.`id` AND profile.`deleted` = b''0''
   SET spu.`category_id` = CASE profile.`publication_type`
       WHEN ''NEWSPAPER'' THEN 90002
       WHEN ''MAGAZINE'' THEN 90003
       WHEN ''TEACHING_AID'' THEN 90004
       ELSE spu.`category_id`
   END
   WHERE profile.`publication_type` IS NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @update_product_spu_category_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_sub_publication_category_profile := (
  SELECT COUNT(*)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sub_publication_category_profile'
);
SET @backfill_supports_gift_from_category_profile_sql := IF(
  @has_sub_publication_category_profile > 0,
  'UPDATE `product_category` category
   INNER JOIN `sub_publication_category_profile` profile ON profile.`category_id` = category.`id`
   SET category.`supports_gift` = profile.`supports_gift`
   WHERE profile.`deleted` = b''0''',
  'SELECT 1'
);
PREPARE stmt FROM @backfill_supports_gift_from_category_profile_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_is_newspaper := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sub_publication_profile'
    AND COLUMN_NAME = 'is_newspaper'
);
SET @backfill_supports_gift_from_is_newspaper_sql := IF(
  @has_is_newspaper > 0,
  'UPDATE `product_category` category
   INNER JOIN `product_spu` spu ON spu.`category_id` = category.`id`
   INNER JOIN `sub_publication_profile` profile ON profile.`product_spu_id` = spu.`id`
   SET category.`supports_gift` = b''1''
   WHERE profile.`deleted` = b''0'' AND profile.`is_newspaper` = b''1''',
  'SELECT 1'
);
PREPARE stmt FROM @backfill_supports_gift_from_is_newspaper_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @drop_publication_profile_sql := IF(
  @has_publication_type > 0 AND @has_is_newspaper > 0,
  'ALTER TABLE `sub_publication_profile` DROP COLUMN `publication_type`, DROP COLUMN `is_newspaper`',
  IF(
    @has_publication_type > 0,
    'ALTER TABLE `sub_publication_profile` DROP COLUMN `publication_type`',
    IF(
      @has_is_newspaper > 0,
      'ALTER TABLE `sub_publication_profile` DROP COLUMN `is_newspaper`',
      'SELECT 1'
    )
  )
);
PREPARE stmt FROM @drop_publication_profile_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

DROP TABLE IF EXISTS `sub_publication_category_profile`;
