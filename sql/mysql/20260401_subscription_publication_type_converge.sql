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

DROP TABLE IF EXISTS `sub_publication_category_profile`;

DELETE FROM `system_role_menu` WHERE `menu_id` IN (5081, 5082);
DELETE FROM `system_menu` WHERE `id` IN (5081, 5082);
