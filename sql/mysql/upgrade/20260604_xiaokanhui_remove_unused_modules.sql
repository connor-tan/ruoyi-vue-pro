-- 清理当前项目不再使用的 CRM / BPM / MES / IoT / ERP 模块入口。
-- 说明：仅清菜单、角色菜单绑定和模块字典；不删除 erp_* 等历史业务表。

DROP TEMPORARY TABLE IF EXISTS tmp_xkh_unused_module_menu_ids;
CREATE TEMPORARY TABLE tmp_xkh_unused_module_menu_ids (
    id BIGINT NOT NULL PRIMARY KEY
) ENGINE = MEMORY;

-- 1. 递归收集目标模块根菜单及子菜单。
INSERT IGNORE INTO tmp_xkh_unused_module_menu_ids (id)
WITH RECURSIVE target_roots AS (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND parent_id = 0
      AND path IN ('/bpm', '/crm', '/erp', '/iot', '/mes')
), target_tree AS (
    SELECT id
    FROM target_roots
    UNION ALL
    SELECT m.id
    FROM system_menu m
             INNER JOIN target_tree t ON m.parent_id = t.id
    WHERE m.deleted = b'0'
)
SELECT id
FROM target_tree;

-- 2. 补充收集权限、组件、路径直接命中的孤立菜单，以及 ERP 标记的 demo03 演示页。
INSERT IGNORE INTO tmp_xkh_unused_module_menu_ids (id)
SELECT id
FROM system_menu
WHERE deleted = b'0'
  AND (
    permission REGEXP '^(crm|bpm|mes|iot|erp):'
        OR component REGEXP '^(crm|bpm|mes|iot|erp)/'
        OR path REGEXP '(^/)?(crm|bpm|mes|iot|erp)(/|$)'
        OR component = 'infra/demo/demo03/erp/index'
        OR path = 'demo03-erp'
  );

-- 3. 删除角色-菜单绑定，再软删除菜单。
DELETE rm
FROM system_role_menu rm
         INNER JOIN tmp_xkh_unused_module_menu_ids t ON rm.menu_id = t.id;

UPDATE system_menu m
    INNER JOIN tmp_xkh_unused_module_menu_ids t ON m.id = t.id
SET m.deleted = b'1',
    m.update_time = NOW(),
    m.updater = '1'
WHERE m.deleted = b'0';

-- 4. 软删除目标模块字典。
UPDATE system_dict_data
SET deleted = b'1',
    update_time = NOW(),
    updater = '1'
WHERE deleted = b'0'
  AND dict_type REGEXP '^(bpm|crm|erp|iot|mes)_';

UPDATE system_dict_type
SET deleted = b'1',
    update_time = NOW(),
    updater = '1'
WHERE deleted = b'0'
  AND type REGEXP '^(bpm|crm|erp|iot|mes)_';

DROP TEMPORARY TABLE IF EXISTS tmp_xkh_unused_module_menu_ids;
