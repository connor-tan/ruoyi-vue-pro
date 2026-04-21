SET @trade_order_item_add_class_id_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'trade_order_item'
        AND column_name = 'subscription_class_id'
    ),
    'SELECT 1',
    'ALTER TABLE `trade_order_item` ADD COLUMN `subscription_class_id` bigint DEFAULT NULL COMMENT ''订刊班级编号'' AFTER `subscription_school_name_snapshot`'
  )
);
PREPARE trade_order_item_add_class_id_stmt FROM @trade_order_item_add_class_id_sql;
EXECUTE trade_order_item_add_class_id_stmt;
DEALLOCATE PREPARE trade_order_item_add_class_id_stmt;

SET @trade_order_item_add_class_name_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'trade_order_item'
        AND column_name = 'subscription_class_name_snapshot'
    ),
    'SELECT 1',
    'ALTER TABLE `trade_order_item` ADD COLUMN `subscription_class_name_snapshot` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT ''订刊班级名称快照'' AFTER `subscription_class_id`'
  )
);
PREPARE trade_order_item_add_class_name_stmt FROM @trade_order_item_add_class_name_sql;
EXECUTE trade_order_item_add_class_name_stmt;
DEALLOCATE PREPARE trade_order_item_add_class_name_stmt;
