-- 校刊汇当前启用模块字段矫正
-- 目标：对齐当前代码中的 DO/Mapper 字段契约，移除会导致插入失败或已无代码承接的残留列。
-- 兼容 MySQL 8：通过 information_schema + PREPARE 保持幂等。

-- 刊物类型 code 已从当前代码与接口中移除，保留会导致新增刊物类型时触发“无默认值”错误。
SET @sql := (
    SELECT IF(COUNT(*) > 0,
              'ALTER TABLE product_publication_type DROP COLUMN code',
              'SELECT ''product_publication_type.code already aligned''')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'product_publication_type'
      AND BINARY COLUMN_NAME = 'code'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 售后订单项字段统一为代码中的 order_item_id，小写 i。
SET @has_old_order_item_column := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'trade_after_sale'
      AND BINARY COLUMN_NAME = 'order_item_Id'
);
SET @has_new_order_item_column := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'trade_after_sale'
      AND BINARY COLUMN_NAME = 'order_item_id'
);
SET @sql := IF(@has_old_order_item_column > 0 AND @has_new_order_item_column = 0,
               'ALTER TABLE trade_after_sale CHANGE COLUMN order_item_Id order_item_id bigint unsigned NOT NULL COMMENT ''订单项编号''',
               'SELECT ''trade_after_sale.order_item_id already aligned''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 商品属性状态已不在当前 DO/Mapper/UI 中使用。
SET @sql := (
    SELECT IF(COUNT(*) > 0,
              'ALTER TABLE product_property DROP COLUMN status',
              'SELECT ''product_property.status already aligned''')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'product_property'
      AND BINARY COLUMN_NAME = 'status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 商品属性值状态已不在当前 DO/Mapper/UI 中使用。
SET @sql := (
    SELECT IF(COUNT(*) > 0,
              'ALTER TABLE product_property_value DROP COLUMN status',
              'SELECT ''product_property_value.status already aligned''')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'product_property_value'
      AND BINARY COLUMN_NAME = 'status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
