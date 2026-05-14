-- 校刊汇：刊物学校配送改为仓库履约
-- 说明：本次不兼容历史订单数据，执行结构切换前清理订单、刊物配送批次和售后数据。

DELETE FROM trade_after_sale_log;
DELETE FROM trade_after_sale;
DELETE FROM trade_publication_delivery_batch_item;
DELETE FROM trade_publication_delivery_batch;
DELETE FROM trade_order_publication_issue;
DELETE FROM trade_order_item;
DELETE FROM trade_order_delivery;
DELETE FROM trade_order_log;
DELETE FROM trade_order;

CREATE TABLE IF NOT EXISTS repo_warehouse (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '仓库编号',
  name varchar(64) NOT NULL COMMENT '仓库名称',
  address varchar(255) NOT NULL COMMENT '仓库地址',
  sort bigint NOT NULL DEFAULT 0 COMMENT '排序',
  remark varchar(512) DEFAULT NULL COMMENT '备注',
  principal varchar(64) DEFAULT NULL COMMENT '负责人',
  warehouse_price decimal(24, 6) DEFAULT NULL COMMENT '仓储费，单位：元',
  truckage_price decimal(24, 6) DEFAULT NULL COMMENT '搬运费，单位：元',
  status tinyint NOT NULL DEFAULT 0 COMMENT '开启状态',
  default_status bit(1) NOT NULL DEFAULT b'0' COMMENT '是否默认',
  creator varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  tenant_id bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (id),
  UNIQUE KEY uk_repo_warehouse_name (name, deleted),
  KEY idx_repo_warehouse_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='仓库';

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'edu_school' AND COLUMN_NAME = 'warehouse_id'
  ),
  'SELECT 1',
  "ALTER TABLE edu_school ADD COLUMN warehouse_id bigint DEFAULT NULL COMMENT '学校配送仓库编号' AFTER station_id"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'edu_school' AND INDEX_NAME = 'idx_edu_school_warehouse_id'
  ),
  'SELECT 1',
  'ALTER TABLE edu_school ADD INDEX idx_edu_school_warehouse_id (warehouse_id)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE system_dict_data
SET label = '学校配送',
    updater = '1',
    update_time = NOW()
WHERE dict_type = 'trade_delivery_type'
  AND value = '3'
  AND deleted = b'0';

-- trade_order_delivery：站点快照切为仓库快照，并补学校地址快照
SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_delivery'
      AND INDEX_NAME = 'idx_trade_order_delivery_station_school'
  ),
  'ALTER TABLE trade_order_delivery DROP INDEX idx_trade_order_delivery_station_school',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_delivery' AND COLUMN_NAME = 'station_id'
  ) AND NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_delivery' AND COLUMN_NAME = 'warehouse_id'
  ),
  "ALTER TABLE trade_order_delivery CHANGE COLUMN station_id warehouse_id bigint DEFAULT NULL COMMENT '学校配送仓库编号'",
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_delivery' AND COLUMN_NAME = 'station_name_snapshot'
  ) AND NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_delivery' AND COLUMN_NAME = 'warehouse_name_snapshot'
  ),
  "ALTER TABLE trade_order_delivery CHANGE COLUMN station_name_snapshot warehouse_name_snapshot varchar(128) DEFAULT NULL COMMENT '学校配送仓库名称快照'",
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_delivery' AND COLUMN_NAME = 'station_address_snapshot'
  ) AND NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_delivery' AND COLUMN_NAME = 'warehouse_address_snapshot'
  ),
  "ALTER TABLE trade_order_delivery CHANGE COLUMN station_address_snapshot warehouse_address_snapshot varchar(255) DEFAULT NULL COMMENT '学校配送仓库地址快照'",
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_delivery' AND COLUMN_NAME = 'school_address_snapshot'
  ),
  'SELECT 1',
  "ALTER TABLE trade_order_delivery ADD COLUMN school_address_snapshot varchar(255) DEFAULT NULL COMMENT '学校地址快照' AFTER school_name_snapshot"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_delivery' AND COLUMN_NAME = 'warehouse_id'
  ),
  'SELECT 1',
  "ALTER TABLE trade_order_delivery ADD COLUMN warehouse_id bigint DEFAULT NULL COMMENT '学校配送仓库编号' AFTER school_address_snapshot"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_delivery' AND COLUMN_NAME = 'warehouse_name_snapshot'
  ),
  'SELECT 1',
  "ALTER TABLE trade_order_delivery ADD COLUMN warehouse_name_snapshot varchar(128) DEFAULT NULL COMMENT '学校配送仓库名称快照' AFTER warehouse_id"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_delivery' AND COLUMN_NAME = 'warehouse_address_snapshot'
  ),
  'SELECT 1',
  "ALTER TABLE trade_order_delivery ADD COLUMN warehouse_address_snapshot varchar(255) DEFAULT NULL COMMENT '学校配送仓库地址快照' AFTER warehouse_name_snapshot"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_delivery'
      AND INDEX_NAME = 'idx_trade_order_delivery_warehouse_school'
  ),
  'SELECT 1',
  'ALTER TABLE trade_order_delivery ADD INDEX idx_trade_order_delivery_warehouse_school (warehouse_id, school_id)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- trade_order_publication_issue：配送候选从站点改为仓库
SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_publication_issue'
      AND INDEX_NAME = 'idx_trade_order_publication_issue_candidate'
  ),
  'ALTER TABLE trade_order_publication_issue DROP INDEX idx_trade_order_publication_issue_candidate',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_publication_issue' AND COLUMN_NAME = 'station_id'
  ) AND NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_publication_issue' AND COLUMN_NAME = 'warehouse_id'
  ),
  "ALTER TABLE trade_order_publication_issue CHANGE COLUMN station_id warehouse_id bigint DEFAULT NULL COMMENT '学校配送仓库编号'",
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_publication_issue' AND COLUMN_NAME = 'station_name_snapshot'
  ) AND NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_publication_issue' AND COLUMN_NAME = 'warehouse_name_snapshot'
  ),
  "ALTER TABLE trade_order_publication_issue CHANGE COLUMN station_name_snapshot warehouse_name_snapshot varchar(128) DEFAULT NULL COMMENT '学校配送仓库名称快照'",
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_publication_issue' AND COLUMN_NAME = 'warehouse_id'
  ),
  'SELECT 1',
  "ALTER TABLE trade_order_publication_issue ADD COLUMN warehouse_id bigint DEFAULT NULL COMMENT '学校配送仓库编号' AFTER class_name_snapshot"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_publication_issue' AND COLUMN_NAME = 'warehouse_name_snapshot'
  ),
  'SELECT 1',
  "ALTER TABLE trade_order_publication_issue ADD COLUMN warehouse_name_snapshot varchar(128) DEFAULT NULL COMMENT '学校配送仓库名称快照' AFTER warehouse_id"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_publication_issue'
      AND INDEX_NAME = 'idx_trade_order_publication_issue_candidate'
  ),
  'SELECT 1',
  'ALTER TABLE trade_order_publication_issue ADD INDEX idx_trade_order_publication_issue_candidate (delivery_type, delivery_status, school_id, warehouse_id, window_id, offer_sku_id, sku_id, issue_no)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- trade_publication_delivery_batch：批次从站点改为仓库
SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_publication_delivery_batch'
      AND INDEX_NAME = 'idx_trade_publication_delivery_batch_candidate'
  ),
  'ALTER TABLE trade_publication_delivery_batch DROP INDEX idx_trade_publication_delivery_batch_candidate',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_publication_delivery_batch' AND COLUMN_NAME = 'station_id'
  ) AND NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_publication_delivery_batch' AND COLUMN_NAME = 'warehouse_id'
  ),
  "ALTER TABLE trade_publication_delivery_batch CHANGE COLUMN station_id warehouse_id bigint DEFAULT NULL COMMENT '学校配送仓库编号'",
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_publication_delivery_batch' AND COLUMN_NAME = 'station_name_snapshot'
  ) AND NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_publication_delivery_batch' AND COLUMN_NAME = 'warehouse_name_snapshot'
  ),
  "ALTER TABLE trade_publication_delivery_batch CHANGE COLUMN station_name_snapshot warehouse_name_snapshot varchar(128) DEFAULT NULL COMMENT '学校配送仓库名称快照'",
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_publication_delivery_batch' AND COLUMN_NAME = 'warehouse_id'
  ),
  'SELECT 1',
  "ALTER TABLE trade_publication_delivery_batch ADD COLUMN warehouse_id bigint DEFAULT NULL COMMENT '学校配送仓库编号' AFTER school_name_snapshot"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_publication_delivery_batch' AND COLUMN_NAME = 'warehouse_name_snapshot'
  ),
  'SELECT 1',
  "ALTER TABLE trade_publication_delivery_batch ADD COLUMN warehouse_name_snapshot varchar(128) DEFAULT NULL COMMENT '学校配送仓库名称快照' AFTER warehouse_id"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_publication_delivery_batch'
      AND INDEX_NAME = 'idx_trade_publication_delivery_batch_candidate'
  ),
  'SELECT 1',
  'ALTER TABLE trade_publication_delivery_batch ADD INDEX idx_trade_publication_delivery_batch_candidate (warehouse_id, school_id, window_id, offer_sku_id, sku_id)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 仓库菜单
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '仓库管理', '', 1, 45, 0, '/repo', 'fa:archive', NULL, NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE path = '/repo' AND deleted = b'0');

SET @repoMenuId := (SELECT id FROM system_menu WHERE path = '/repo' AND deleted = b'0' LIMIT 1);

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '仓库维护', 'repo:warehouse:query', 2, 1, @repoMenuId, 'warehouse', 'fa:archive',
       'repo/warehouse/index', 'RepoWarehouse', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @repoMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'repo:warehouse:query' AND deleted = b'0');

SET @warehouseMenuId := (
  SELECT id FROM system_menu WHERE permission = 'repo:warehouse:query' AND deleted = b'0' LIMIT 1
);

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '仓库新增', 'repo:warehouse:create', 3, 1, @warehouseMenuId, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @warehouseMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'repo:warehouse:create' AND deleted = b'0');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '仓库修改', 'repo:warehouse:update', 3, 2, @warehouseMenuId, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @warehouseMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'repo:warehouse:update' AND deleted = b'0');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '仓库删除', 'repo:warehouse:delete', 3, 3, @warehouseMenuId, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @warehouseMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'repo:warehouse:delete' AND deleted = b'0');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '仓库导出', 'repo:warehouse:export', 3, 4, @warehouseMenuId, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @warehouseMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'repo:warehouse:export' AND deleted = b'0');
