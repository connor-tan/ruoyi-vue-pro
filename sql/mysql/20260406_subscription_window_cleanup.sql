SET NAMES utf8mb4;

DELETE FROM `system_role_menu`
WHERE `menu_id` IN (5070, 5071, 5072, 5073, 5074, 5075, 5076, 5077, 5078, 5079, 5080, 5081, 5082, 5083, 5084);

DELETE FROM `system_menu`
WHERE `id` IN (5070, 5071, 5072, 5073, 5074, 5075, 5076, 5077, 5078, 5079, 5080, 5081, 5082, 5083, 5084);

DROP TABLE IF EXISTS `sub_grade_catalog_property_value_map`;
DROP TABLE IF EXISTS `sub_publication_series_spu`;
DROP TABLE IF EXISTS `sub_publication_series`;
DROP TABLE IF EXISTS `sub_publication_category_profile`;
DROP TABLE IF EXISTS `sub_publication_attr`;
DROP TABLE IF EXISTS `sub_publication_profile`;
DROP TABLE IF EXISTS `sub_window_publication_rule`;
DROP TABLE IF EXISTS `sub_window_publication_grade`;
DROP TABLE IF EXISTS `sub_window_publication`;

SET @supports_gift_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'product_category'
      AND column_name = 'supports_gift'
);
SET @drop_supports_gift_sql = IF(@supports_gift_exists > 0,
                                 'ALTER TABLE `product_category` DROP COLUMN `supports_gift`',
                                 'SELECT 1');
PREPARE stmt FROM @drop_supports_gift_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
