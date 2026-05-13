-- 后台手动/导入订单：订单来源、线下收款字典和权限

SET @column_exists := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'trade_order'
      AND COLUMN_NAME = 'order_source'
);
SET @ddl := IF(@column_exists = 0,
  "ALTER TABLE trade_order ADD COLUMN order_source varchar(32) NOT NULL DEFAULT 'APP' COMMENT '业务订单来源：APP/ADMIN_MANUAL/ADMIN_IMPORT' AFTER terminal",
  "SELECT 1");
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE trade_order
SET order_source = 'APP'
WHERE order_source IS NULL OR order_source = '';

SET @column_not_nullable := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'trade_order'
      AND COLUMN_NAME = 'user_id'
      AND IS_NULLABLE = 'NO'
);
SET @ddl := IF(@column_not_nullable > 0,
  "ALTER TABLE trade_order MODIFY COLUMN user_id bigint unsigned NULL COMMENT '用户编号'",
  "SELECT 1");
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_not_nullable := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'trade_order_item'
      AND COLUMN_NAME = 'user_id'
      AND IS_NULLABLE = 'NO'
);
SET @ddl := IF(@column_not_nullable > 0,
  "ALTER TABLE trade_order_item MODIFY COLUMN user_id bigint unsigned NULL COMMENT '用户编号'",
  "SELECT 1");
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_not_nullable := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'trade_order_publication_issue'
      AND COLUMN_NAME = 'user_id'
      AND IS_NULLABLE = 'NO'
);
SET @ddl := IF(@column_not_nullable > 0,
  "ALTER TABLE trade_order_publication_issue MODIFY COLUMN user_id bigint NULL COMMENT '用户编号'",
  "SELECT 1");
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists := (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'trade_order'
      AND INDEX_NAME = 'idx_trade_order_order_source'
);
SET @ddl := IF(@index_exists = 0,
  "ALTER TABLE trade_order ADD INDEX idx_trade_order_order_source (order_source, status, create_time)",
  "SELECT 1");
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists := (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'trade_order_item'
      AND INDEX_NAME = 'idx_trade_order_item_student_offer_sku'
);
SET @ddl := IF(@index_exists = 0,
  "ALTER TABLE trade_order_item ADD INDEX idx_trade_order_item_student_offer_sku (subscription_student_id, subscription_offer_sku_id)",
  "SELECT 1");
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted)
SELECT '交易订单业务来源', 'trade_order_source', 0, '区分 APP 下单、后台手动新建和后台批量导入订单', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM system_dict_type WHERE type = 'trade_order_source' AND deleted = b'0'
);

INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 1, 'APP 下单', 'APP', 'trade_order_source', 0, 'primary', '', 'APP 侧用户下单', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM system_dict_data WHERE dict_type = 'trade_order_source' AND value = 'APP' AND deleted = b'0'
);
INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 2, '后台手动新建', 'ADMIN_MANUAL', 'trade_order_source', 0, 'warning', '', '管理后台手动创建订单', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM system_dict_data WHERE dict_type = 'trade_order_source' AND value = 'ADMIN_MANUAL' AND deleted = b'0'
);
INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 3, '后台批量导入', 'ADMIN_IMPORT', 'trade_order_source', 0, 'success', '', '管理后台批量导入订单', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM system_dict_data WHERE dict_type = 'trade_order_source' AND value = 'ADMIN_IMPORT' AND deleted = b'0'
);

INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 40, '管理后台', '40', 'terminal', 0, 'default', '', '终端 - 管理后台', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM system_dict_data WHERE dict_type = 'terminal' AND value = '40' AND deleted = b'0'
);

INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 22, '线下收款', 'offline', 'pay_channel_code', 0, 'warning', '', '后台手动确认线下收款', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM system_dict_data WHERE dict_type = 'pay_channel_code' AND value = 'offline' AND deleted = b'0'
);

SET @order_menu_id := (
    SELECT parent_id
    FROM system_menu
    WHERE permission = 'trade:order:query' AND deleted = b'0'
    ORDER BY id DESC
    LIMIT 1
);

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '订单创建', 'trade:order:create', 3, 3, @order_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @order_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'trade:order:create' AND deleted = b'0');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '订单导入', 'trade:order:import', 3, 4, @order_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @order_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'trade:order:import' AND deleted = b'0');
