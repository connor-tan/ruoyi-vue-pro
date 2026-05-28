-- 校刊汇：清理 manager 角色中误授予的会员中心菜单范围
-- 规则：manager 的菜单范围以 system_role_menu 授权为准，但不展示会员中心入口；
--      学生管理页面后端查询家长仍需要 member:user:query，因此保留该按钮权限。

CREATE TEMPORARY TABLE tmp_xkh_manager_member_menu_cleanup (
    menu_id BIGINT PRIMARY KEY
) ENGINE = MEMORY;

INSERT IGNORE INTO tmp_xkh_manager_member_menu_cleanup (menu_id)
WITH RECURSIVE member_menu_tree AS (
    SELECT id, parent_id, permission
    FROM system_menu
    WHERE deleted = 0
      AND parent_id = 0
      AND path = '/member'
      AND name = '会员中心'
    UNION ALL
    SELECT child.id, child.parent_id, child.permission
    FROM system_menu child
    INNER JOIN member_menu_tree parent ON child.parent_id = parent.id
    WHERE child.deleted = 0
)
SELECT id
FROM member_menu_tree
WHERE COALESCE(permission, '') <> 'member:user:query';

UPDATE system_role_menu rm
INNER JOIN system_role r ON r.id = rm.role_id
INNER JOIN tmp_xkh_manager_member_menu_cleanup t ON t.menu_id = rm.menu_id
SET rm.deleted = b'1',
    rm.updater = '1',
    rm.update_time = NOW()
WHERE r.code = 'manager'
  AND r.deleted = b'0'
  AND rm.deleted = b'0';

SELECT COUNT(*) AS active_manager_member_menu_count
FROM system_role_menu rm
INNER JOIN system_role r ON r.id = rm.role_id
INNER JOIN system_menu m ON m.id = rm.menu_id
WHERE r.code = 'manager'
  AND r.deleted = b'0'
  AND rm.deleted = b'0'
  AND m.deleted = b'0'
  AND m.id IN (SELECT menu_id FROM tmp_xkh_manager_member_menu_cleanup);

SELECT COUNT(*) AS active_manager_member_user_query_count
FROM system_role_menu rm
INNER JOIN system_role r ON r.id = rm.role_id
INNER JOIN system_menu m ON m.id = rm.menu_id
WHERE r.code = 'manager'
  AND r.deleted = b'0'
  AND rm.deleted = b'0'
  AND m.deleted = b'0'
  AND m.permission = 'member:user:query';

DROP TEMPORARY TABLE tmp_xkh_manager_member_menu_cleanup;

-- 允许 manager 进入“菜单管理”查看自身范围内的菜单；仅补查询入口，不补新增、修改、删除。
CREATE TEMPORARY TABLE tmp_xkh_manager_menu_query_scope (
    menu_id BIGINT PRIMARY KEY
) ENGINE = MEMORY;

INSERT IGNORE INTO tmp_xkh_manager_menu_query_scope (menu_id)
SELECT id
FROM system_menu
WHERE deleted = 0
  AND (
        (parent_id = 1 AND name = '菜单管理' AND path = 'menu' AND component = 'system/menu/index')
        OR permission = 'system:menu:query'
      );

UPDATE system_role_menu rm
INNER JOIN system_role r ON r.id = rm.role_id
INNER JOIN tmp_xkh_manager_menu_query_scope t ON t.menu_id = rm.menu_id
SET rm.deleted = b'0',
    rm.updater = '1',
    rm.update_time = NOW()
WHERE r.code = 'manager'
  AND r.deleted = b'0';

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted)
SELECT r.id,
       t.menu_id,
       '1',
       NOW(),
       '1',
       NOW(),
       b'0'
FROM system_role r
INNER JOIN tmp_xkh_manager_menu_query_scope t
WHERE r.code = 'manager'
  AND r.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1
      FROM system_role_menu rm
      WHERE rm.role_id = r.id
        AND rm.menu_id = t.menu_id
  );

SELECT COUNT(*) AS active_manager_menu_query_scope_count
FROM system_role_menu rm
INNER JOIN system_role r ON r.id = rm.role_id
INNER JOIN tmp_xkh_manager_menu_query_scope t ON t.menu_id = rm.menu_id
WHERE r.code = 'manager'
  AND r.deleted = b'0'
  AND rm.deleted = b'0';

DROP TEMPORARY TABLE tmp_xkh_manager_menu_query_scope;
