SET NAMES utf8mb4;

DELETE FROM `system_role_menu` WHERE `menu_id` IN (5070, 5071, 5072);
DELETE FROM `system_menu` WHERE `id` IN (5070, 5071, 5072);

DROP TABLE IF EXISTS `sub_publication_attr`;
