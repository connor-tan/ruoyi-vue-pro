-- 20260609 业务审查修复：站点履约维度、禁用自提入口与普通商品配送收束。

-- trade_order_publication_issue：恢复站点事实，避免同校不同站点候选/批次合并。
SET @sql := IF(
  NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_publication_issue' AND COLUMN_NAME = 'station_id'
  ),
  'ALTER TABLE trade_order_publication_issue ADD COLUMN station_id bigint DEFAULT NULL COMMENT ''站点编号'' AFTER school_name_snapshot',
  'SELECT ''trade_order_publication_issue.station_id exists'' AS message'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_publication_issue' AND COLUMN_NAME = 'station_name_snapshot'
  ),
  'ALTER TABLE trade_order_publication_issue ADD COLUMN station_name_snapshot varchar(128) DEFAULT NULL COMMENT ''站点名称快照'' AFTER station_id',
  'SELECT ''trade_order_publication_issue.station_name_snapshot exists'' AS message'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE trade_order_publication_issue pi
LEFT JOIN edu_school s ON s.id = pi.school_id AND s.deleted = b'0'
LEFT JOIN edu_station st ON st.id = s.station_id AND st.deleted = b'0'
SET pi.station_id = s.station_id,
    pi.station_name_snapshot = st.station_name
WHERE pi.deleted = b'0'
  AND pi.station_id IS NULL
  AND pi.school_id IS NOT NULL;

SET @sql := IF(
  EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_publication_issue'
      AND INDEX_NAME = 'idx_trade_order_publication_issue_candidate'
  ),
  'ALTER TABLE trade_order_publication_issue DROP INDEX idx_trade_order_publication_issue_candidate',
  'SELECT ''idx_trade_order_publication_issue_candidate not exists'' AS message'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

ALTER TABLE trade_order_publication_issue
  ADD INDEX idx_trade_order_publication_issue_candidate (
    delivery_type, delivery_status, school_id, station_id, warehouse_id,
    window_id, offer_sku_id, sku_id, issue_no
  );

-- repo_publication_delivery_batch：批次追溯补站点维度。
SET @sql := IF(
  NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'repo_publication_delivery_batch' AND COLUMN_NAME = 'station_id'
  ),
  'ALTER TABLE repo_publication_delivery_batch ADD COLUMN station_id bigint DEFAULT NULL COMMENT ''站点编号'' AFTER school_name_snapshot',
  'SELECT ''repo_publication_delivery_batch.station_id exists'' AS message'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'repo_publication_delivery_batch' AND COLUMN_NAME = 'station_name_snapshot'
  ),
  'ALTER TABLE repo_publication_delivery_batch ADD COLUMN station_name_snapshot varchar(128) DEFAULT NULL COMMENT ''站点名称快照'' AFTER station_id',
  'SELECT ''repo_publication_delivery_batch.station_name_snapshot exists'' AS message'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE repo_publication_delivery_batch b
LEFT JOIN edu_school s ON s.id = b.school_id AND s.deleted = b'0'
LEFT JOIN edu_station st ON st.id = s.station_id AND st.deleted = b'0'
SET b.station_id = s.station_id,
    b.station_name_snapshot = st.station_name
WHERE b.deleted = b'0'
  AND b.station_id IS NULL
  AND b.school_id IS NOT NULL;

SET @sql := IF(
  EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'repo_publication_delivery_batch'
      AND INDEX_NAME = 'idx_repo_publication_delivery_batch_candidate'
  ),
  'ALTER TABLE repo_publication_delivery_batch DROP INDEX idx_repo_publication_delivery_batch_candidate',
  'SELECT ''idx_repo_publication_delivery_batch_candidate not exists'' AS message'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

ALTER TABLE repo_publication_delivery_batch
  ADD INDEX idx_repo_publication_delivery_batch_candidate (
    warehouse_id, school_id, station_id, window_id, offer_sku_id, sku_id
  );

-- 自提不在当前业务范围：保留技术残留，关闭新入口和展示入口。
UPDATE trade_config
SET delivery_pick_up_enabled = b'0',
    updater = '1',
    update_time = NOW()
WHERE deleted = b'0'
  AND delivery_pick_up_enabled <> b'0';

UPDATE system_menu
SET status = 1,
    visible = b'0',
    updater = '1',
    update_time = NOW()
WHERE deleted = b'0'
  AND (
    id IN (2166, 2179, 2180, 2181, 2182, 2183, 2184, 2389)
    OR permission LIKE '%pick-up%'
    OR permission LIKE 'trade:delivery:pick-up-store:%'
    OR permission LIKE 'trade:delivery:pick-up-order:%'
    OR path LIKE '%pick-up%'
    OR path LIKE '%pickUp%'
    OR component LIKE '%pick-up%'
    OR component LIKE '%pickUp%'
    OR component_name LIKE '%PickUp%'
    OR name LIKE '%自提%'
  );

UPDATE system_dict_data
SET status = 1,
    updater = '1',
    update_time = NOW()
WHERE deleted = b'0'
  AND dict_type = 'trade_delivery_type'
  AND value = '2';

UPDATE product_spu
SET delivery_types = '[1]',
    updater = '1',
    update_time = NOW()
WHERE deleted = b'0'
  AND biz_scene = 'NORMAL'
  AND delivery_types IS NOT NULL
  AND JSON_VALID(delivery_types)
  AND JSON_CONTAINS(CAST(delivery_types AS JSON), CAST('2' AS JSON), '$');
