-- 校刊汇：仓库侧刊物供应商、收货与发货余额占用

CREATE TABLE IF NOT EXISTS repo_supplier (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '供应商编号',
  name varchar(128) NOT NULL COMMENT '供应商名称',
  code varchar(64) DEFAULT NULL COMMENT '供应商编码',
  contact_name varchar(64) DEFAULT NULL COMMENT '联系人',
  contact_mobile varchar(32) DEFAULT NULL COMMENT '联系电话',
  address varchar(255) DEFAULT NULL COMMENT '地址',
  sort bigint NOT NULL DEFAULT 0 COMMENT '排序',
  status tinyint NOT NULL DEFAULT 0 COMMENT '开启状态',
  remark varchar(512) DEFAULT NULL COMMENT '备注',
  creator varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  tenant_id bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (id),
  UNIQUE KEY uk_repo_supplier_name (name, deleted),
  UNIQUE KEY uk_repo_supplier_code (code, deleted),
  KEY idx_repo_supplier_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='仓库供应商';

CREATE TABLE IF NOT EXISTS repo_supplier_publication_sku (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '刊物供应关系编号',
  supplier_id bigint NOT NULL COMMENT '供应商编号',
  spu_id bigint NOT NULL COMMENT '商品 SPU 编号',
  sku_id bigint NOT NULL COMMENT '商品 SKU 编号',
  product_name_snapshot varchar(255) DEFAULT NULL COMMENT '刊物名称快照',
  product_sku_name_snapshot varchar(255) DEFAULT NULL COMMENT '商品 SKU 名称快照',
  isbn varchar(64) DEFAULT NULL COMMENT 'ISBN',
  status tinyint NOT NULL DEFAULT 0 COMMENT '开启状态',
  sort bigint NOT NULL DEFAULT 0 COMMENT '排序',
  remark varchar(512) DEFAULT NULL COMMENT '备注',
  creator varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  tenant_id bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (id),
  UNIQUE KEY uk_repo_supplier_publication_sku (supplier_id, sku_id, deleted),
  KEY idx_repo_supplier_publication_sku_sku (sku_id),
  KEY idx_repo_supplier_publication_sku_spu (spu_id),
  KEY idx_repo_supplier_publication_sku_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='仓库刊物供应商关系';

CREATE TABLE IF NOT EXISTS repo_publication_receipt (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '收货单编号',
  receipt_no varchar(64) NOT NULL COMMENT '收货单号',
  supplier_id bigint NOT NULL COMMENT '供应商编号',
  supplier_name_snapshot varchar(128) DEFAULT NULL COMMENT '供应商名称快照',
  warehouse_id bigint NOT NULL COMMENT '仓库编号',
  warehouse_name_snapshot varchar(128) DEFAULT NULL COMMENT '仓库名称快照',
  status int NOT NULL DEFAULT 10 COMMENT '收货单状态',
  expected_count int NOT NULL DEFAULT 0 COMMENT '应收数量',
  received_count int NOT NULL DEFAULT 0 COMMENT '已到货数量',
  allocated_count int NOT NULL DEFAULT 0 COMMENT '已出库占用数量',
  submit_time datetime DEFAULT NULL COMMENT '提交时间',
  close_time datetime DEFAULT NULL COMMENT '关闭时间',
  close_reason varchar(512) DEFAULT NULL COMMENT '关闭原因',
  remark varchar(512) DEFAULT NULL COMMENT '备注',
  creator varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  tenant_id bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (id),
  UNIQUE KEY uk_repo_publication_receipt_no (receipt_no),
  KEY idx_repo_publication_receipt_supplier (supplier_id),
  KEY idx_repo_publication_receipt_warehouse (warehouse_id),
  KEY idx_repo_publication_receipt_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='刊物收货单';

CREATE TABLE IF NOT EXISTS repo_publication_receipt_item (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '收货明细编号',
  receipt_id bigint NOT NULL COMMENT '收货单编号',
  supplier_id bigint NOT NULL COMMENT '供应商编号',
  warehouse_id bigint NOT NULL COMMENT '仓库编号',
  window_id bigint NOT NULL COMMENT '订刊窗口编号',
  window_name_snapshot varchar(128) DEFAULT NULL COMMENT '订刊窗口名称快照',
  offer_id bigint NOT NULL COMMENT '订刊窗口刊物编号',
  offer_sku_id bigint NOT NULL COMMENT '订刊窗口 SKU 编号',
  spu_id bigint NOT NULL COMMENT '商品 SPU 编号',
  sku_id bigint NOT NULL COMMENT '商品 SKU 编号',
  product_name_snapshot varchar(255) DEFAULT NULL COMMENT '刊物名称快照',
  product_sku_name_snapshot varchar(255) DEFAULT NULL COMMENT '商品 SKU 名称快照',
  isbn varchar(64) DEFAULT NULL COMMENT 'ISBN',
  issue_id bigint DEFAULT NULL COMMENT '订刊期次编号',
  issue_no int NOT NULL COMMENT '期号',
  issue_name varchar(128) DEFAULT NULL COMMENT '期次名称',
  expected_count int NOT NULL DEFAULT 0 COMMENT '应收数量',
  received_count int NOT NULL DEFAULT 0 COMMENT '已到货数量',
  allocated_count int NOT NULL DEFAULT 0 COMMENT '已出库占用数量',
  remark varchar(512) DEFAULT NULL COMMENT '备注',
  creator varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  tenant_id bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (id),
  KEY idx_repo_publication_receipt_item_receipt (receipt_id),
  KEY idx_repo_publication_receipt_item_balance (warehouse_id, window_id, offer_id, offer_sku_id, sku_id, issue_no),
  KEY idx_repo_publication_receipt_item_supplier_sku (supplier_id, sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='刊物收货明细';

CREATE TABLE IF NOT EXISTS repo_publication_receipt_record (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '收货记录编号',
  receipt_id bigint NOT NULL COMMENT '收货单编号',
  receipt_item_id bigint NOT NULL COMMENT '收货明细编号',
  received_time datetime NOT NULL COMMENT '收货时间',
  bundle_count int DEFAULT NULL COMMENT '捆数',
  received_count int NOT NULL COMMENT '本次收货数量',
  operator_user_id bigint DEFAULT NULL COMMENT '操作人',
  remark varchar(512) DEFAULT NULL COMMENT '备注',
  creator varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  tenant_id bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (id),
  KEY idx_repo_publication_receipt_record_receipt (receipt_id),
  KEY idx_repo_publication_receipt_record_item (receipt_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='刊物收货记录';

CREATE TABLE IF NOT EXISTS repo_publication_receipt_allocation (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '出库占用编号',
  receipt_item_id bigint NOT NULL COMMENT '收货明细编号',
  delivery_batch_id bigint NOT NULL COMMENT '刊物发货批次编号',
  allocated_count int NOT NULL COMMENT '占用数量',
  operator_user_id bigint DEFAULT NULL COMMENT '操作人',
  delivery_time datetime NOT NULL COMMENT '发货时间',
  remark varchar(512) DEFAULT NULL COMMENT '备注',
  creator varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  tenant_id bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (id),
  KEY idx_repo_publication_receipt_allocation_item (receipt_item_id),
  KEY idx_repo_publication_receipt_allocation_batch (delivery_batch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='刊物出库占用流水';

-- 字典：刊物收货单状态
SET @dict_type := CONVERT('repo_publication_receipt_status' USING utf8mb4) COLLATE utf8mb4_unicode_ci;

UPDATE system_dict_type
SET name = '刊物收货单状态',
    status = 0,
    remark = '仓库刊物收货单状态',
    deleted = b'0',
    updater = '1',
    update_time = NOW()
WHERE type = @dict_type;

INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted)
SELECT '刊物收货单状态', @dict_type, 0, '仓库刊物收货单状态', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_type WHERE type = @dict_type);

UPDATE system_dict_data SET deleted = b'0', updater = '1', update_time = NOW()
WHERE dict_type = @dict_type AND value IN ('10', '20', '30', '40', '50');

INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark,
                              creator, create_time, updater, update_time, deleted)
SELECT 1, '草稿', '10', @dict_type, 0, 'info', '', '', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = @dict_type AND value = '10');

INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark,
                              creator, create_time, updater, update_time, deleted)
SELECT 2, '待收货', '20', @dict_type, 0, 'primary', '', '', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = @dict_type AND value = '20');

INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark,
                              creator, create_time, updater, update_time, deleted)
SELECT 3, '部分收货', '30', @dict_type, 0, 'warning', '', '', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = @dict_type AND value = '30');

INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark,
                              creator, create_time, updater, update_time, deleted)
SELECT 4, '已收齐', '40', @dict_type, 0, 'success', '', '', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = @dict_type AND value = '40');

INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark,
                              creator, create_time, updater, update_time, deleted)
SELECT 5, '已关闭', '50', @dict_type, 0, 'info', '', '', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = @dict_type AND value = '50');

-- 菜单
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '仓库管理', '', 1, 45, 0, '/repo', 'fa:archive', NULL, NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE path = '/repo' AND deleted = b'0');

SET @repoMenuId := (SELECT id FROM system_menu WHERE path = '/repo' AND deleted = b'0' LIMIT 1);

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '供应商维护', 'repo:supplier:query', 2, 2, @repoMenuId, 'supplier', 'fa:address-book',
       'repo/supplier/index', 'RepoSupplier', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @repoMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'repo:supplier:query' AND deleted = b'0');

SET @supplierMenuId := (SELECT id FROM system_menu WHERE permission = 'repo:supplier:query' AND deleted = b'0' LIMIT 1);

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '供应商新增', 'repo:supplier:create', 3, 1, @supplierMenuId, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @supplierMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'repo:supplier:create' AND deleted = b'0');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '供应商修改', 'repo:supplier:update', 3, 2, @supplierMenuId, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @supplierMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'repo:supplier:update' AND deleted = b'0');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '供应商删除', 'repo:supplier:delete', 3, 3, @supplierMenuId, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @supplierMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'repo:supplier:delete' AND deleted = b'0');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '供应商导出', 'repo:supplier:export', 3, 4, @supplierMenuId, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @supplierMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'repo:supplier:export' AND deleted = b'0');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '供应刊物查询', 'repo:supplier-publication-sku:query', 3, 5, @supplierMenuId, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @supplierMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'repo:supplier-publication-sku:query' AND deleted = b'0');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '供应刊物新增', 'repo:supplier-publication-sku:create', 3, 6, @supplierMenuId, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @supplierMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'repo:supplier-publication-sku:create' AND deleted = b'0');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '供应刊物修改', 'repo:supplier-publication-sku:update', 3, 7, @supplierMenuId, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @supplierMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'repo:supplier-publication-sku:update' AND deleted = b'0');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '供应刊物删除', 'repo:supplier-publication-sku:delete', 3, 8, @supplierMenuId, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @supplierMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'repo:supplier-publication-sku:delete' AND deleted = b'0');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '刊物收货', 'repo:publication-receipt:query', 2, 3, @repoMenuId, 'publication-receipt', 'fa:truck',
       'repo/publicationReceipt/index', 'RepoPublicationReceipt', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @repoMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'repo:publication-receipt:query' AND deleted = b'0');

SET @receiptMenuId := (
  SELECT id FROM system_menu WHERE permission = 'repo:publication-receipt:query' AND deleted = b'0' LIMIT 1
);

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '刊物收货新增', 'repo:publication-receipt:create', 3, 1, @receiptMenuId, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @receiptMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'repo:publication-receipt:create' AND deleted = b'0');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '刊物收货修改', 'repo:publication-receipt:update', 3, 2, @receiptMenuId, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @receiptMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'repo:publication-receipt:update' AND deleted = b'0');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '刊物收货登记', 'repo:publication-receipt:receive', 3, 3, @receiptMenuId, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @receiptMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'repo:publication-receipt:receive' AND deleted = b'0');

SELECT COUNT(*) AS repo_publication_receipt_table_count
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN (
    'repo_supplier',
    'repo_supplier_publication_sku',
    'repo_publication_receipt',
    'repo_publication_receipt_item',
    'repo_publication_receipt_record',
    'repo_publication_receipt_allocation'
  );

SELECT COUNT(*) AS repo_publication_receipt_page_menu_count
FROM system_menu
WHERE deleted = b'0'
  AND permission IN (
    'repo:supplier:query',
    'repo:publication-receipt:query'
  );

SELECT COUNT(*) AS repo_supplier_publication_button_count
FROM system_menu
WHERE deleted = b'0'
  AND parent_id = @supplierMenuId
  AND permission IN (
    'repo:supplier-publication-sku:query',
    'repo:supplier-publication-sku:create',
    'repo:supplier-publication-sku:update',
    'repo:supplier-publication-sku:delete'
  );
