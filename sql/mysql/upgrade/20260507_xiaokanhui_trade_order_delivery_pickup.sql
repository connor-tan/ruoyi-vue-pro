-- 将普通商品自提履约事实下沉到配送组，支持 MIXED 主订单中的 PICK_UP 配送组核销。

SET @sql = IF(
  NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_delivery' AND COLUMN_NAME = 'pick_up_store_id'
  ),
  "ALTER TABLE trade_order_delivery ADD COLUMN pick_up_store_id bigint DEFAULT NULL COMMENT '自提门店编号' AFTER receiver_detail_address",
  "SELECT 1"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_delivery' AND COLUMN_NAME = 'pick_up_verify_code'
  ),
  "ALTER TABLE trade_order_delivery ADD COLUMN pick_up_verify_code varchar(32) DEFAULT NULL COMMENT '自提核销码' AFTER pick_up_store_id",
  "SELECT 1"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  NOT EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_delivery' AND INDEX_NAME = 'uk_trade_order_delivery_pick_up_verify_code'
  ),
  "ALTER TABLE trade_order_delivery ADD UNIQUE KEY uk_trade_order_delivery_pick_up_verify_code (pick_up_verify_code)",
  "SELECT 1"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
