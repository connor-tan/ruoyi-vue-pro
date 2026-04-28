-- 校刊汇数据库定义清理
-- 本脚本只做不改变主业务行为的 DDL 调整：删除无运行时引用的旧刊物模型、清理旧业务域字段、修正审计字段类型。

SET @schema_name := DATABASE();

DROP TABLE IF EXISTS product_sku_publication;
DROP TABLE IF EXISTS product_spu_publication;
DROP TABLE IF EXISTS product_publication_title_identifier;
DROP TABLE IF EXISTS product_publication_publisher;

SET @sql := IF(
  (SELECT COUNT(1)
     FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'product_spu'
      AND COLUMN_NAME = 'domain_type') > 0,
  'ALTER TABLE product_spu DROP COLUMN domain_type',
  'SELECT ''skip product_spu.domain_type'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
  (SELECT COUNT(1)
     FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'product_sku'
      AND COLUMN_NAME = 'updater') > 0,
  'ALTER TABLE product_sku MODIFY COLUMN updater varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT ''更新人''',
  'SELECT ''skip product_sku.updater'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
