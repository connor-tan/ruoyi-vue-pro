-- 校刊汇 manager 学生管理家长搜索权限修复
-- 目标：学生管理页面初始化需要调用 /member/user/page 搜索家长，仅恢复 member:user:query 按钮权限，不开放会员管理菜单入口。

SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS xiaokanhui_fix_manager_student_parent_query_permission_20260528;

DELIMITER $$

CREATE PROCEDURE xiaokanhui_fix_manager_student_parent_query_permission_20260528()
BEGIN
    DECLARE v_manager_role_exists int DEFAULT 0;
    DECLARE v_member_query_menu_exists int DEFAULT 0;
    DECLARE v_active_member_query_count int DEFAULT 0;
    DECLARE v_active_member_menu_count int DEFAULT 0;
    DECLARE v_updated_rows int DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT COUNT(*)
      INTO v_manager_role_exists
      FROM system_role
     WHERE id = 160
       AND code = 'manager'
       AND status = 0
       AND deleted = b'0';

    IF v_manager_role_exists <> 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'manager 学生管理权限修复失败：manager 角色不存在或未启用';
    END IF;

    SELECT COUNT(*)
      INTO v_member_query_menu_exists
      FROM system_menu
     WHERE id = 2318
       AND permission = 'member:user:query'
       AND type = 3
       AND status = 0
       AND deleted = b'0';

    IF v_member_query_menu_exists <> 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'manager 学生管理权限修复失败：member:user:query 菜单按钮不存在或未启用';
    END IF;

    SELECT COUNT(*)
      INTO v_active_member_query_count
      FROM system_role_menu
     WHERE role_id = 160
       AND menu_id = 2318
       AND deleted = b'0';

    IF v_active_member_query_count = 0 THEN
        UPDATE system_role_menu
           SET deleted = b'0',
               updater = 'admin',
               update_time = NOW()
         WHERE id = (
             SELECT id
               FROM (
                    SELECT id
                      FROM system_role_menu
                     WHERE role_id = 160
                       AND menu_id = 2318
                     ORDER BY id
                     LIMIT 1
               ) t
         );

        SET v_updated_rows = ROW_COUNT();

        IF v_updated_rows = 0 THEN
            INSERT INTO system_role_menu (
                role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                160, 2318, 'admin', NOW(), 'admin', NOW(), b'0', 0
            );
        END IF;
    END IF;

    SELECT COUNT(*)
      INTO v_active_member_query_count
      FROM system_role_menu
     WHERE role_id = 160
       AND menu_id = 2318
       AND deleted = b'0';

    IF v_active_member_query_count < 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'manager 学生管理权限修复失败：member:user:query 未成功授权';
    END IF;

    SELECT COUNT(*)
      INTO v_active_member_menu_count
      FROM system_role_menu
     WHERE role_id = 160
       AND menu_id = 2317
       AND deleted = b'0';

    IF v_active_member_menu_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'manager 学生管理权限修复失败：会员管理菜单入口被启用';
    END IF;

    COMMIT;

    SELECT v_active_member_query_count AS active_member_user_query_permission_count,
           v_active_member_menu_count AS active_member_user_menu_count;
END $$

DELIMITER ;

CALL xiaokanhui_fix_manager_student_parent_query_permission_20260528();

DROP PROCEDURE IF EXISTS xiaokanhui_fix_manager_student_parent_query_permission_20260528;
