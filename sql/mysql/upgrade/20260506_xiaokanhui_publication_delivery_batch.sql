-- 校刊汇：刊物站点批次发货

CREATE TABLE IF NOT EXISTS trade_publication_delivery_batch (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '批次编号',
  batch_no varchar(32) NOT NULL COMMENT '批次号',
  school_id bigint NOT NULL COMMENT '学校编号',
  school_name_snapshot varchar(128) DEFAULT NULL COMMENT '学校名称快照',
  station_id bigint NOT NULL COMMENT '站点编号',
  station_name_snapshot varchar(128) DEFAULT NULL COMMENT '站点名称快照',
  window_id bigint NOT NULL COMMENT '订刊窗口编号',
  window_name_snapshot varchar(128) DEFAULT NULL COMMENT '订刊窗口名称快照',
  offer_id bigint NOT NULL COMMENT '订刊窗口刊物编号',
  offer_sku_id bigint NOT NULL COMMENT '订刊窗口 SKU 编号',
  sku_id bigint NOT NULL COMMENT '商品 SKU 编号',
  product_name_snapshot varchar(255) DEFAULT NULL COMMENT '刊物商品名称快照',
  total_count int NOT NULL DEFAULT 0 COMMENT '本批次数量',
  order_count int NOT NULL DEFAULT 0 COMMENT '涉及订单数',
  student_count int NOT NULL DEFAULT 0 COMMENT '涉及学生数',
  status int NOT NULL COMMENT '批次状态',
  delivery_time datetime NOT NULL COMMENT '发货时间',
  operator_user_id bigint DEFAULT NULL COMMENT '操作人',
  remark varchar(512) DEFAULT NULL COMMENT '备注',
  creator varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_trade_publication_delivery_batch_no (batch_no),
  KEY idx_trade_publication_delivery_batch_candidate (station_id, school_id, window_id, offer_sku_id, sku_id),
  KEY idx_trade_publication_delivery_batch_time (delivery_time)
) COMMENT='刊物站点发货批次';

CREATE TABLE IF NOT EXISTS trade_publication_delivery_batch_item (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '明细编号',
  batch_id bigint NOT NULL COMMENT '批次编号',
  order_id bigint NOT NULL COMMENT '订单编号',
  order_no varchar(64) DEFAULT NULL COMMENT '订单号快照',
  order_item_id bigint NOT NULL COMMENT '订单项编号',
  delivery_id bigint NOT NULL COMMENT '配送组编号',
  user_id bigint NOT NULL COMMENT '用户编号',
  count int NOT NULL COMMENT '商品数量',
  student_id bigint DEFAULT NULL COMMENT '学生编号',
  student_name_snapshot varchar(64) DEFAULT NULL COMMENT '学生名称快照',
  class_id bigint DEFAULT NULL COMMENT '班级编号',
  class_name_snapshot varchar(128) DEFAULT NULL COMMENT '班级名称快照',
  creator varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_trade_publication_delivery_batch_item_order_item (order_item_id),
  KEY idx_trade_publication_delivery_batch_item_batch (batch_id),
  KEY idx_trade_publication_delivery_batch_item_delivery (delivery_id)
) COMMENT='刊物站点发货批次明细';

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_item' AND COLUMN_NAME = 'publication_delivery_status'
  ),
  'SELECT 1',
  "ALTER TABLE trade_order_item ADD COLUMN publication_delivery_status int NOT NULL DEFAULT 10 COMMENT '刊物发货状态'"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_item' AND COLUMN_NAME = 'publication_delivery_batch_id'
  ),
  'SELECT 1',
  "ALTER TABLE trade_order_item ADD COLUMN publication_delivery_batch_id bigint DEFAULT NULL COMMENT '刊物发货批次编号'"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_item' AND COLUMN_NAME = 'publication_delivery_time'
  ),
  'SELECT 1',
  "ALTER TABLE trade_order_item ADD COLUMN publication_delivery_time datetime DEFAULT NULL COMMENT '刊物发货时间'"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_item' AND INDEX_NAME = 'idx_trade_order_item_publication_delivery'
  ),
  'SELECT 1',
  'ALTER TABLE trade_order_item ADD INDEX idx_trade_order_item_publication_delivery (publication_delivery_status, subscription_offer_sku_id, sku_id)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_item' AND INDEX_NAME = 'idx_trade_order_item_delivery_publication'
  ),
  'SELECT 1',
  'ALTER TABLE trade_order_item ADD INDEX idx_trade_order_item_delivery_publication (delivery_id, publication_delivery_status)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 管理端菜单：订单中心 / 刊物批次发货
SET @batchMenuId = (SELECT id FROM system_menu WHERE component = 'mall/trade/publicationDeliveryBatch/index' LIMIT 1);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '刊物批次发货', '', 2, 3, 2072, 'publication-delivery-batch', 'ep:box', 'mall/trade/publicationDeliveryBatch/index', 'TradePublicationDeliveryBatch', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @batchMenuId IS NULL;

SET @batchMenuId = (SELECT id FROM system_menu WHERE component = 'mall/trade/publicationDeliveryBatch/index' LIMIT 1);

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '刊物批次查询', 'trade:publication-delivery-batch:query', 3, 1, @batchMenuId, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @batchMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'trade:publication-delivery-batch:query');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '刊物批次发货', 'trade:publication-delivery-batch:create', 3, 2, @batchMenuId, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @batchMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'trade:publication-delivery-batch:create');
