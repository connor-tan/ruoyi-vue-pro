-- 刊物商品配送能力收口
-- 刊物商品不再维护独立 fulfillment_mode，统一使用 product_spu.delivery_types 表达可支持配送方式。

SET @schema_name := DATABASE();

SET @sql := IF(
  (SELECT COUNT(1)
     FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'product_publication_spu_ext'
      AND COLUMN_NAME = 'fulfillment_mode') > 0,
  'ALTER TABLE product_publication_spu_ext DROP COLUMN fulfillment_mode',
  'SELECT ''skip product_publication_spu_ext.fulfillment_mode'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
