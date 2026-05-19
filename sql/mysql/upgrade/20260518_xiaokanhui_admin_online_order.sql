-- 后台在线订刊下单：订单来源字典

SET @column_exists := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'trade_order'
      AND COLUMN_NAME = 'order_source'
);
SET @ddl := IF(@column_exists > 0,
  "ALTER TABLE trade_order MODIFY COLUMN order_source varchar(32) NOT NULL DEFAULT 'APP' COMMENT '业务订单来源：APP/ADMIN_MANUAL/ADMIN_IMPORT/ADMIN_ONLINE'",
  "SELECT 1");
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE system_dict_type
SET remark = '区分 APP 下单、后台手动新建、后台批量导入和后台在线下单订单',
    update_time = NOW()
WHERE type = 'trade_order_source'
  AND deleted = b'0';

INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 4, '后台在线下单', 'ADMIN_ONLINE', 'trade_order_source', 0, 'success', '', '管理后台代家长在线下单并走真实支付', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM system_dict_data WHERE dict_type = 'trade_order_source' AND value = 'ADMIN_ONLINE' AND deleted = b'0'
);
