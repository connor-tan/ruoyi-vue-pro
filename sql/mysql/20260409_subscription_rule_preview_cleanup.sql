SET NAMES utf8mb4;

DELETE FROM `system_role_menu`
WHERE `menu_id` = 5080;

DELETE FROM `system_menu`
WHERE `id` = 5080
   OR `permission` = 'subscription:preview:query';
