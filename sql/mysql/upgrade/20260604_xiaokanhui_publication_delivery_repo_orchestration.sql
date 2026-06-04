-- 校刊汇：刊物发货业务入口与出库批次迁移到仓库域

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS repo_publication_delivery_batch (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '批次编号',
  batch_no varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '批次号',
  delivery_type int NOT NULL COMMENT '配送方式',
  school_id bigint DEFAULT NULL COMMENT '学校编号',
  school_name_snapshot varchar(128) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '学校名称快照',
  warehouse_id bigint DEFAULT NULL COMMENT '学校配送仓库编号',
  warehouse_name_snapshot varchar(128) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '学校配送仓库名称快照',
  window_id bigint NOT NULL COMMENT '订刊窗口编号',
  window_name_snapshot varchar(128) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '订刊窗口名称快照',
  offer_id bigint NOT NULL COMMENT '订刊窗口刊物编号',
  offer_sku_id bigint NOT NULL COMMENT '订刊窗口 SKU 编号',
  sku_id bigint NOT NULL COMMENT '商品 SKU 编号',
  product_name_snapshot varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '刊物商品名称快照',
  issue_id bigint DEFAULT NULL COMMENT '订刊期次编号',
  issue_no int NOT NULL COMMENT '期号',
  issue_name varchar(128) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '期次名称',
  total_count int NOT NULL COMMENT '本批次数量',
  order_count int NOT NULL COMMENT '涉及订单数',
  student_count int NOT NULL COMMENT '涉及学生数',
  status int NOT NULL COMMENT '批次状态',
  delivery_time datetime NOT NULL COMMENT '发货时间',
  operator_user_id bigint DEFAULT NULL COMMENT '操作人',
  remark varchar(512) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '备注',
  creator varchar(64) COLLATE utf8mb4_bin DEFAULT '' COMMENT '创建者',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater varchar(64) COLLATE utf8mb4_bin DEFAULT '' COMMENT '更新者',
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_repo_publication_delivery_batch_no (batch_no),
  KEY idx_repo_publication_delivery_batch_candidate (warehouse_id, school_id, window_id, offer_sku_id, sku_id),
  KEY idx_repo_publication_delivery_batch_issue (delivery_type, issue_no, offer_sku_id, sku_id),
  KEY idx_repo_publication_delivery_batch_time (delivery_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='仓库刊物出库发货批次';

CREATE TABLE IF NOT EXISTS repo_publication_delivery_batch_item (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '明细编号',
  batch_id bigint NOT NULL COMMENT '批次编号',
  order_id bigint NOT NULL COMMENT '订单编号',
  order_no varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '订单号快照',
  order_item_id bigint NOT NULL COMMENT '订单项编号',
  order_issue_id bigint NOT NULL COMMENT '订单刊物期次编号',
  delivery_id bigint NOT NULL COMMENT '配送组编号',
  user_id bigint NOT NULL COMMENT '用户编号',
  count int NOT NULL COMMENT '商品数量',
  issue_no int NOT NULL COMMENT '期号',
  issue_name varchar(128) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '期次名称',
  logistics_id bigint DEFAULT NULL COMMENT '物流公司编号',
  logistics_no varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '物流单号',
  student_id bigint DEFAULT NULL COMMENT '学生编号',
  student_name_snapshot varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '学生名称快照',
  class_id bigint DEFAULT NULL COMMENT '班级编号',
  class_name_snapshot varchar(128) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '班级名称快照',
  creator varchar(64) COLLATE utf8mb4_bin DEFAULT '' COMMENT '创建者',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater varchar(64) COLLATE utf8mb4_bin DEFAULT '' COMMENT '更新者',
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_repo_publication_delivery_batch_item_order_issue (order_issue_id),
  KEY idx_repo_publication_delivery_batch_item_batch (batch_id),
  KEY idx_repo_publication_delivery_batch_item_delivery (delivery_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='仓库刊物出库发货批次明细';

-- 历史批次迁移：保留原 ID，使 trade_order_publication_issue.delivery_batch_id 继续可追溯到 repo 批次。
INSERT IGNORE INTO repo_publication_delivery_batch (
    id, batch_no, delivery_type, school_id, school_name_snapshot, warehouse_id, warehouse_name_snapshot,
    window_id, window_name_snapshot, offer_id, offer_sku_id, sku_id, product_name_snapshot,
    issue_id, issue_no, issue_name, total_count, order_count, student_count, status, delivery_time,
    operator_user_id, remark, creator, create_time, updater, update_time, deleted
)
SELECT id, batch_no, delivery_type, school_id, school_name_snapshot, warehouse_id, warehouse_name_snapshot,
       window_id, window_name_snapshot, offer_id, offer_sku_id, sku_id, product_name_snapshot,
       issue_id, issue_no, issue_name, total_count, order_count, student_count, status, delivery_time,
       operator_user_id, remark, creator, create_time, updater, update_time, deleted
FROM trade_publication_delivery_batch
WHERE EXISTS (
    SELECT 1
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'trade_publication_delivery_batch'
);

INSERT IGNORE INTO repo_publication_delivery_batch_item (
    id, batch_id, order_id, order_no, order_item_id, order_issue_id, delivery_id, user_id, count,
    issue_no, issue_name, logistics_id, logistics_no, student_id, student_name_snapshot, class_id,
    class_name_snapshot, creator, create_time, updater, update_time, deleted
)
SELECT id, batch_id, order_id, order_no, order_item_id, order_issue_id, delivery_id, user_id, count,
       issue_no, issue_name, logistics_id, logistics_no, student_id, student_name_snapshot, class_id,
       class_name_snapshot, creator, create_time, updater, update_time, deleted
FROM trade_publication_delivery_batch_item
WHERE EXISTS (
    SELECT 1
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'trade_publication_delivery_batch_item'
);

SET @repo_delivery_batch_next_id := (
  SELECT GREATEST(COALESCE(MAX(id), 0) + 1, 1) FROM repo_publication_delivery_batch
);
SET @repo_delivery_batch_auto_sql := CONCAT('ALTER TABLE repo_publication_delivery_batch AUTO_INCREMENT = ', @repo_delivery_batch_next_id);
PREPARE repo_delivery_batch_auto_stmt FROM @repo_delivery_batch_auto_sql;
EXECUTE repo_delivery_batch_auto_stmt;
DEALLOCATE PREPARE repo_delivery_batch_auto_stmt;

SET @repo_delivery_batch_item_next_id := (
  SELECT GREATEST(COALESCE(MAX(id), 0) + 1, 1) FROM repo_publication_delivery_batch_item
);
SET @repo_delivery_batch_item_auto_sql := CONCAT('ALTER TABLE repo_publication_delivery_batch_item AUTO_INCREMENT = ', @repo_delivery_batch_item_next_id);
PREPARE repo_delivery_batch_item_auto_stmt FROM @repo_delivery_batch_item_auto_sql;
EXECUTE repo_delivery_batch_item_auto_stmt;
DEALLOCATE PREPARE repo_delivery_batch_item_auto_stmt;

-- 菜单与权限
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '仓库管理', '', 1, 45, 0, '/repo', 'fa:archive', NULL, NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE path = '/repo' AND deleted = b'0');

SET @repoMenuId := (SELECT id FROM system_menu WHERE path = '/repo' AND deleted = b'0' LIMIT 1);

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '刊物发货', 'repo:publication-delivery-batch:query', 2, 4, @repoMenuId,
       'publication-delivery-batch', 'ep:box', 'repo/publicationDeliveryBatch/index',
       'RepoPublicationDeliveryBatch', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @repoMenuId IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM system_menu
    WHERE permission = 'repo:publication-delivery-batch:query'
      AND type = 2
      AND deleted = b'0'
  );

SET @repoDeliveryMenuId := (
  SELECT id
  FROM system_menu
  WHERE permission = 'repo:publication-delivery-batch:query'
    AND type = 2
    AND deleted = b'0'
  LIMIT 1
);

UPDATE system_menu
SET name = '刊物发货',
    parent_id = @repoMenuId,
    path = 'publication-delivery-batch',
    icon = 'ep:box',
    component = 'repo/publicationDeliveryBatch/index',
    component_name = 'RepoPublicationDeliveryBatch',
    sort = 4,
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    updater = '1',
    update_time = NOW()
WHERE @repoDeliveryMenuId IS NOT NULL
  AND id = @repoDeliveryMenuId;

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '刊物发货', 'repo:publication-delivery-batch:create', 3, 1, @repoDeliveryMenuId, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @repoDeliveryMenuId IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM system_menu
    WHERE permission = 'repo:publication-delivery-batch:create'
      AND deleted = b'0'
  );

SET @repoDeliveryCreateMenuId := (
  SELECT id
  FROM system_menu
  WHERE permission = 'repo:publication-delivery-batch:create'
    AND deleted = b'0'
  LIMIT 1
);

UPDATE system_menu
SET parent_id = @repoDeliveryMenuId,
    type = 3,
    name = '刊物发货',
    sort = 1,
    path = '',
    icon = '',
    component = '',
    component_name = '',
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    updater = '1',
    update_time = NOW()
WHERE @repoDeliveryCreateMenuId IS NOT NULL
  AND id = @repoDeliveryCreateMenuId;

DROP TEMPORARY TABLE IF EXISTS tmp_xkh_publication_delivery_old_menu_ids;
CREATE TEMPORARY TABLE tmp_xkh_publication_delivery_old_menu_ids (
  id bigint NOT NULL PRIMARY KEY,
  target_menu_id bigint NOT NULL
) ENGINE = MEMORY;

-- 旧页面菜单和旧查询按钮都映射到新页面菜单，因为新页面菜单本身持有 query 权限。
INSERT IGNORE INTO tmp_xkh_publication_delivery_old_menu_ids (id, target_menu_id)
SELECT id, @repoDeliveryMenuId
FROM system_menu
WHERE @repoDeliveryMenuId IS NOT NULL
  AND deleted = b'0'
  AND (
    (type = 2 AND (component = 'mall/trade/publicationDeliveryBatch/index'
        OR component_name = 'TradePublicationDeliveryBatch'
        OR name = '刊物批次发货'))
    OR permission = 'trade:publication-delivery-batch:query'
  );

INSERT IGNORE INTO tmp_xkh_publication_delivery_old_menu_ids (id, target_menu_id)
SELECT id, @repoDeliveryCreateMenuId
FROM system_menu
WHERE @repoDeliveryCreateMenuId IS NOT NULL
  AND deleted = b'0'
  AND permission = 'trade:publication-delivery-batch:create';

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted)
SELECT rm.role_id,
       old_menu.target_menu_id,
       '1',
       NOW(),
       '1',
       NOW(),
       b'0'
FROM system_role_menu rm
INNER JOIN tmp_xkh_publication_delivery_old_menu_ids old_menu ON old_menu.id = rm.menu_id
WHERE rm.deleted = b'0'
  AND NOT EXISTS (
    SELECT 1
    FROM system_role_menu existed_rm
    WHERE existed_rm.role_id = rm.role_id
      AND existed_rm.menu_id = old_menu.target_menu_id
  );

UPDATE system_role_menu target_rm
INNER JOIN system_role_menu old_rm ON old_rm.role_id = target_rm.role_id
INNER JOIN tmp_xkh_publication_delivery_old_menu_ids old_menu ON old_menu.id = old_rm.menu_id
SET target_rm.deleted = b'0',
    target_rm.updater = '1',
    target_rm.update_time = NOW()
WHERE target_rm.menu_id = old_menu.target_menu_id
  AND old_rm.deleted = b'0';

DELETE rm
FROM system_role_menu rm
INNER JOIN tmp_xkh_publication_delivery_old_menu_ids old_menu ON old_menu.id = rm.menu_id;

UPDATE system_menu m
INNER JOIN tmp_xkh_publication_delivery_old_menu_ids old_menu ON old_menu.id = m.id
SET m.deleted = b'1',
    m.updater = '1',
    m.update_time = NOW()
WHERE m.deleted = b'0';

SELECT COUNT(*) AS repo_publication_delivery_table_count
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN (
    'repo_publication_delivery_batch',
    'repo_publication_delivery_batch_item'
  );

SELECT COUNT(*) AS active_repo_publication_delivery_menu_count
FROM system_menu
WHERE deleted = b'0'
  AND permission IN (
    'repo:publication-delivery-batch:query',
    'repo:publication-delivery-batch:create'
  );

SELECT COUNT(*) AS active_trade_publication_delivery_menu_count
FROM system_menu
WHERE deleted = b'0'
  AND (
    permission IN (
      'trade:publication-delivery-batch:query',
      'trade:publication-delivery-batch:create'
    )
    OR component = 'mall/trade/publicationDeliveryBatch/index'
    OR component_name = 'TradePublicationDeliveryBatch'
  );

SELECT COUNT(*) AS active_role_binding_to_old_publication_delivery_menu_count
FROM system_role_menu rm
INNER JOIN tmp_xkh_publication_delivery_old_menu_ids old_menu ON old_menu.id = rm.menu_id
WHERE rm.deleted = b'0';

DROP TEMPORARY TABLE IF EXISTS tmp_xkh_publication_delivery_old_menu_ids;
