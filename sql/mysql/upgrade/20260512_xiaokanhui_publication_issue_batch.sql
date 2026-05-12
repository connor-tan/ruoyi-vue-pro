-- 校刊汇：刊物期次批次管理

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'product_publication_spu_ext' AND COLUMN_NAME = 'issue_mode'
  ),
  'SELECT 1',
  "ALTER TABLE product_publication_spu_ext ADD COLUMN issue_mode varchar(32) NOT NULL DEFAULT 'SINGLE' COMMENT '期次模式：PERIODICAL 期刊，SINGLE 独立刊物' AFTER publication_type_id"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE product_publication_spu_ext
SET issue_mode = 'SINGLE'
WHERE issue_mode IS NULL OR issue_mode = '';

CREATE TABLE IF NOT EXISTS subscription_offer_sku_issue (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '期次编号',
  offer_id bigint NOT NULL COMMENT '订刊窗口刊物编号',
  offer_sku_id bigint NOT NULL COMMENT '订刊窗口 SKU 编号',
  issue_no int NOT NULL COMMENT '期号',
  issue_name varchar(128) NOT NULL COMMENT '期次名称',
  planned_publish_date date DEFAULT NULL COMMENT '计划出刊日期',
  planned_delivery_date date DEFAULT NULL COMMENT '计划配送日期',
  sort int NOT NULL DEFAULT 0 COMMENT '排序',
  status tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  remark varchar(255) DEFAULT NULL COMMENT '备注',
  creator varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  active_issue_no int GENERATED ALWAYS AS (CASE WHEN deleted = b'0' THEN issue_no ELSE NULL END) STORED COMMENT '启用唯一期号',
  PRIMARY KEY (id),
  UNIQUE KEY uk_subscription_offer_sku_issue_no (offer_sku_id, active_issue_no),
  KEY idx_subscription_offer_sku_issue_offer (offer_id),
  KEY idx_subscription_offer_sku_issue_offer_sku (offer_sku_id, status, sort)
) COMMENT='订刊窗口 SKU 期次计划';

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_item' AND COLUMN_NAME = 'publication_issue_mode'
  ),
  'SELECT 1',
  "ALTER TABLE trade_order_item ADD COLUMN publication_issue_mode varchar(32) DEFAULT NULL COMMENT '刊物期次模式' AFTER publication_delivery_time"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_item' AND COLUMN_NAME = 'publication_issue_total_count'
  ),
  'SELECT 1',
  "ALTER TABLE trade_order_item ADD COLUMN publication_issue_total_count int NOT NULL DEFAULT 0 COMMENT '刊物总期数' AFTER publication_issue_mode"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_item' AND COLUMN_NAME = 'publication_issue_delivered_count'
  ),
  'SELECT 1',
  "ALTER TABLE trade_order_item ADD COLUMN publication_issue_delivered_count int NOT NULL DEFAULT 0 COMMENT '刊物已发期数' AFTER publication_issue_total_count"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_item' AND COLUMN_NAME = 'publication_issue_received_count'
  ),
  'SELECT 1',
  "ALTER TABLE trade_order_item ADD COLUMN publication_issue_received_count int NOT NULL DEFAULT 0 COMMENT '刊物已收期数' AFTER publication_issue_delivered_count"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_item' AND COLUMN_NAME = 'publication_fulfillment_status'
  ),
  'SELECT 1',
  "ALTER TABLE trade_order_item ADD COLUMN publication_fulfillment_status int DEFAULT NULL COMMENT '刊物期次履约状态' AFTER publication_issue_received_count"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_item' AND INDEX_NAME = 'idx_trade_order_item_publication_issue'
  ),
  'SELECT 1',
  'ALTER TABLE trade_order_item ADD INDEX idx_trade_order_item_publication_issue (publication_fulfillment_status, subscription_offer_sku_id, sku_id)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS trade_order_publication_issue (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '订单刊物期次编号',
  order_id bigint NOT NULL COMMENT '订单编号',
  order_no varchar(64) DEFAULT NULL COMMENT '订单号快照',
  order_item_id bigint NOT NULL COMMENT '订单项编号',
  delivery_id bigint NOT NULL COMMENT '配送组编号',
  user_id bigint NOT NULL COMMENT '用户编号',
  delivery_type int NOT NULL COMMENT '配送方式',
  spu_id bigint NOT NULL COMMENT '商品 SPU 编号',
  sku_id bigint NOT NULL COMMENT '商品 SKU 编号',
  product_name_snapshot varchar(255) DEFAULT NULL COMMENT '刊物商品名称快照',
  count int NOT NULL DEFAULT 1 COMMENT '购买数量',
  student_id bigint DEFAULT NULL COMMENT '学生编号',
  student_name_snapshot varchar(64) DEFAULT NULL COMMENT '学生名称快照',
  school_id bigint DEFAULT NULL COMMENT '学校编号',
  school_name_snapshot varchar(128) DEFAULT NULL COMMENT '学校名称快照',
  class_id bigint DEFAULT NULL COMMENT '班级编号',
  class_name_snapshot varchar(128) DEFAULT NULL COMMENT '班级名称快照',
  station_id bigint DEFAULT NULL COMMENT '站点编号',
  station_name_snapshot varchar(128) DEFAULT NULL COMMENT '站点名称快照',
  window_id bigint NOT NULL COMMENT '订刊窗口编号',
  window_name_snapshot varchar(128) DEFAULT NULL COMMENT '订刊窗口名称快照',
  target_period varchar(32) DEFAULT NULL COMMENT '目标周期',
  offer_id bigint NOT NULL COMMENT '订刊窗口刊物编号',
  offer_sku_id bigint NOT NULL COMMENT '订刊窗口 SKU 编号',
  issue_id bigint DEFAULT NULL COMMENT '订刊期次编号',
  issue_no int NOT NULL COMMENT '期号',
  issue_name varchar(128) NOT NULL COMMENT '期次名称',
  planned_publish_date date DEFAULT NULL COMMENT '计划出刊日期',
  planned_delivery_date date DEFAULT NULL COMMENT '计划配送日期',
  delivery_status int NOT NULL DEFAULT 10 COMMENT '期次发货状态',
  receive_status int NOT NULL DEFAULT 10 COMMENT '期次收货状态',
  canceled bit(1) NOT NULL DEFAULT b'0' COMMENT '是否取消',
  delivery_batch_id bigint DEFAULT NULL COMMENT '发货批次编号',
  delivery_time datetime DEFAULT NULL COMMENT '发货时间',
  logistics_id bigint DEFAULT NULL COMMENT '物流公司编号',
  logistics_no varchar(64) DEFAULT NULL COMMENT '物流单号',
  receive_time datetime DEFAULT NULL COMMENT '收货时间',
  receiver_user_id bigint DEFAULT NULL COMMENT '收货用户编号',
  creator varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  active_issue_no int GENERATED ALWAYS AS (CASE WHEN deleted = b'0' THEN issue_no ELSE NULL END) STORED COMMENT '启用唯一期号',
  PRIMARY KEY (id),
  UNIQUE KEY uk_trade_order_publication_issue_item_issue (order_item_id, active_issue_no),
  KEY idx_trade_order_publication_issue_order (order_id),
  KEY idx_trade_order_publication_issue_delivery (delivery_id, delivery_status, receive_status),
  KEY idx_trade_order_publication_issue_candidate (delivery_type, delivery_status, school_id, station_id, window_id, offer_sku_id, sku_id, issue_no),
  KEY idx_trade_order_publication_issue_receive (delivery_status, receive_status, delivery_time)
) COMMENT='交易订单刊物期次履约事实';

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_publication_delivery_batch' AND COLUMN_NAME = 'delivery_type'
  ),
  'SELECT 1',
  "ALTER TABLE trade_publication_delivery_batch ADD COLUMN delivery_type int NOT NULL DEFAULT 3 COMMENT '配送方式' AFTER batch_no"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_publication_delivery_batch' AND COLUMN_NAME = 'issue_id'
  ),
  'SELECT 1',
  "ALTER TABLE trade_publication_delivery_batch ADD COLUMN issue_id bigint DEFAULT NULL COMMENT '订刊期次编号' AFTER target_period"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_publication_delivery_batch' AND COLUMN_NAME = 'issue_no'
  ),
  'SELECT 1',
  "ALTER TABLE trade_publication_delivery_batch ADD COLUMN issue_no int DEFAULT NULL COMMENT '期号' AFTER issue_id"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_publication_delivery_batch' AND COLUMN_NAME = 'issue_name'
  ),
  'SELECT 1',
  "ALTER TABLE trade_publication_delivery_batch ADD COLUMN issue_name varchar(128) DEFAULT NULL COMMENT '期次名称' AFTER issue_no"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_publication_delivery_batch'
  ),
  "ALTER TABLE trade_publication_delivery_batch MODIFY station_id bigint DEFAULT NULL COMMENT '站点编号'",
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_publication_delivery_batch_item' AND COLUMN_NAME = 'order_issue_id'
  ),
  'SELECT 1',
  "ALTER TABLE trade_publication_delivery_batch_item ADD COLUMN order_issue_id bigint DEFAULT NULL COMMENT '订单刊物期次编号' AFTER order_item_id"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

DELETE FROM trade_publication_delivery_batch_item WHERE order_issue_id IS NULL;
DELETE b
FROM trade_publication_delivery_batch b
LEFT JOIN trade_publication_delivery_batch_item bi ON bi.batch_id = b.id
WHERE bi.id IS NULL;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'trade_publication_delivery_batch_item'
      AND COLUMN_NAME = 'order_issue_id'
      AND IS_NULLABLE = 'YES'
  ),
  "ALTER TABLE trade_publication_delivery_batch_item MODIFY order_issue_id bigint NOT NULL COMMENT '订单刊物期次编号'",
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_publication_delivery_batch_item' AND COLUMN_NAME = 'issue_no'
  ),
  'SELECT 1',
  "ALTER TABLE trade_publication_delivery_batch_item ADD COLUMN issue_no int DEFAULT NULL COMMENT '期号' AFTER count"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_publication_delivery_batch_item' AND COLUMN_NAME = 'issue_name'
  ),
  'SELECT 1',
  "ALTER TABLE trade_publication_delivery_batch_item ADD COLUMN issue_name varchar(128) DEFAULT NULL COMMENT '期次名称' AFTER issue_no"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_publication_delivery_batch_item' AND COLUMN_NAME = 'logistics_id'
  ),
  'SELECT 1',
  "ALTER TABLE trade_publication_delivery_batch_item ADD COLUMN logistics_id bigint DEFAULT NULL COMMENT '物流公司编号' AFTER issue_name"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_publication_delivery_batch_item' AND COLUMN_NAME = 'logistics_no'
  ),
  'SELECT 1',
  "ALTER TABLE trade_publication_delivery_batch_item ADD COLUMN logistics_no varchar(64) DEFAULT NULL COMMENT '物流单号' AFTER logistics_id"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_publication_delivery_batch_item' AND INDEX_NAME = 'uk_trade_publication_delivery_batch_item_order_item'
  ),
  'ALTER TABLE trade_publication_delivery_batch_item DROP INDEX uk_trade_publication_delivery_batch_item_order_item',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_publication_delivery_batch_item' AND INDEX_NAME = 'uk_trade_publication_delivery_batch_item_order_issue'
  ),
  'SELECT 1',
  'ALTER TABLE trade_publication_delivery_batch_item ADD UNIQUE KEY uk_trade_publication_delivery_batch_item_order_issue (order_issue_id)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_publication_delivery_batch' AND INDEX_NAME = 'idx_trade_publication_delivery_batch_issue'
  ),
  'SELECT 1',
  'ALTER TABLE trade_publication_delivery_batch ADD INDEX idx_trade_publication_delivery_batch_issue (delivery_type, issue_no, offer_sku_id, sku_id)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
