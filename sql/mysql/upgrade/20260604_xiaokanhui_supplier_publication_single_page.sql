-- 校刊汇：供应商维护与刊物供应商合并为单页入口

SET NAMES utf8mb4;

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

UPDATE system_menu
SET name = '供应商维护',
    parent_id = @repoMenuId,
    path = 'supplier',
    icon = 'fa:address-book',
    component = 'repo/supplier/index',
    component_name = 'RepoSupplier',
    type = 2,
    sort = 2,
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    updater = '1',
    update_time = NOW()
WHERE @supplierMenuId IS NOT NULL
  AND id = @supplierMenuId;

UPDATE system_menu
SET parent_id = @supplierMenuId,
    type = 3,
    name = CASE permission
             WHEN 'repo:supplier-publication-sku:query' THEN '供应刊物查询'
             WHEN 'repo:supplier-publication-sku:create' THEN '供应刊物新增'
             WHEN 'repo:supplier-publication-sku:update' THEN '供应刊物修改'
             WHEN 'repo:supplier-publication-sku:delete' THEN '供应刊物删除'
             ELSE name
           END,
    sort = CASE permission
             WHEN 'repo:supplier-publication-sku:query' THEN 5
             WHEN 'repo:supplier-publication-sku:create' THEN 6
             WHEN 'repo:supplier-publication-sku:update' THEN 7
             WHEN 'repo:supplier-publication-sku:delete' THEN 8
             ELSE sort
           END,
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
WHERE @supplierMenuId IS NOT NULL
  AND deleted = b'0'
  AND permission IN (
    'repo:supplier-publication-sku:query',
    'repo:supplier-publication-sku:create',
    'repo:supplier-publication-sku:update',
    'repo:supplier-publication-sku:delete'
  );

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '供应刊物查询', 'repo:supplier-publication-sku:query', 3, 5, @supplierMenuId, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @supplierMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'repo:supplier-publication-sku:query' AND deleted = b'0');

SET @supplierPublicationQueryMenuId := (
  SELECT id
  FROM system_menu
  WHERE permission = 'repo:supplier-publication-sku:query'
    AND parent_id = @supplierMenuId
    AND type = 3
    AND deleted = b'0'
  LIMIT 1
);

UPDATE system_role_menu target_rm
INNER JOIN system_role_menu supplier_rm ON supplier_rm.role_id = target_rm.role_id
SET target_rm.deleted = b'0',
    target_rm.updater = '1',
    target_rm.update_time = NOW()
WHERE @supplierPublicationQueryMenuId IS NOT NULL
  AND supplier_rm.menu_id = @supplierMenuId
  AND supplier_rm.deleted = b'0'
  AND target_rm.menu_id = @supplierPublicationQueryMenuId;

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted)
SELECT supplier_rm.role_id,
       @supplierPublicationQueryMenuId,
       '1',
       NOW(),
       '1',
       NOW(),
       b'0'
FROM system_role_menu supplier_rm
WHERE @supplierPublicationQueryMenuId IS NOT NULL
  AND supplier_rm.menu_id = @supplierMenuId
  AND supplier_rm.deleted = b'0'
  AND NOT EXISTS (
    SELECT 1
    FROM system_role_menu existed_rm
    WHERE existed_rm.role_id = supplier_rm.role_id
      AND existed_rm.menu_id = @supplierPublicationQueryMenuId
  );

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

UPDATE system_menu
SET sort = 3,
    updater = '1',
    update_time = NOW()
WHERE permission = 'repo:publication-receipt:query'
  AND deleted = b'0';

DROP TEMPORARY TABLE IF EXISTS tmp_xkh_supplier_publication_old_menu_ids;
CREATE TEMPORARY TABLE tmp_xkh_supplier_publication_old_menu_ids (
    id BIGINT NOT NULL PRIMARY KEY
) ENGINE = MEMORY;

INSERT IGNORE INTO tmp_xkh_supplier_publication_old_menu_ids (id)
SELECT id
FROM system_menu
WHERE deleted = b'0'
  AND type = 2
  AND (
    permission = 'repo:supplier-publication-sku:query'
        OR component = 'repo/supplierPublicationSku/index'
        OR path = 'supplier-publication-sku'
        OR name = '刊物供应商'
  );

DELETE rm
FROM system_role_menu rm
INNER JOIN tmp_xkh_supplier_publication_old_menu_ids old_menu ON old_menu.id = rm.menu_id;

UPDATE system_menu m
INNER JOIN tmp_xkh_supplier_publication_old_menu_ids old_menu ON old_menu.id = m.id
SET m.deleted = b'1',
    m.updater = '1',
    m.update_time = NOW()
WHERE m.deleted = b'0';

SELECT COUNT(*) AS active_supplier_publication_page_menu_count
FROM system_menu
WHERE deleted = b'0'
  AND type = 2
  AND (
    permission = 'repo:supplier-publication-sku:query'
        OR component = 'repo/supplierPublicationSku/index'
        OR path = 'supplier-publication-sku'
        OR name = '刊物供应商'
  );

SELECT COUNT(*) AS supplier_publication_button_under_supplier_count
FROM system_menu
WHERE deleted = b'0'
  AND parent_id = @supplierMenuId
  AND permission IN (
    'repo:supplier-publication-sku:query',
    'repo:supplier-publication-sku:create',
    'repo:supplier-publication-sku:update',
    'repo:supplier-publication-sku:delete'
  );

SELECT COUNT(*) AS supplier_publication_query_role_binding_count
FROM system_role_menu
WHERE @supplierPublicationQueryMenuId IS NOT NULL
  AND menu_id = @supplierPublicationQueryMenuId
  AND deleted = b'0';

SELECT COUNT(*) AS active_role_binding_to_old_supplier_publication_menu_count
FROM system_role_menu rm
INNER JOIN tmp_xkh_supplier_publication_old_menu_ids old_menu ON old_menu.id = rm.menu_id;

DROP TEMPORARY TABLE IF EXISTS tmp_xkh_supplier_publication_old_menu_ids;
