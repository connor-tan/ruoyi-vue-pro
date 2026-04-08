SET NAMES utf8mb4;

DELETE FROM `system_role_menu` WHERE `menu_id` IN (5070, 5071, 5072, 5073, 5074, 5075, 5076, 5077, 5078, 5079, 5080, 5081, 5082, 5083, 5084, 5085);
DELETE FROM `system_menu` WHERE `id` IN (5070, 5071, 5072, 5073, 5074, 5075, 5076, 5077, 5078, 5079, 5080, 5081, 5082, 5083, 5084, 5085);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(5066, '订刊规则中心', '', 2, 10, 5065, 'subscription-rule-center', 'ep:reading', 'subscription/rule-center/index', 'SubscriptionRuleCenter',
 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0')
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`permission` = VALUES(`permission`),
`type` = VALUES(`type`),
`sort` = VALUES(`sort`),
`parent_id` = VALUES(`parent_id`),
`path` = VALUES(`path`),
`icon` = VALUES(`icon`),
`component` = VALUES(`component`),
`component_name` = VALUES(`component_name`),
`status` = VALUES(`status`),
`visible` = VALUES(`visible`),
`keep_alive` = VALUES(`keep_alive`),
`always_show` = VALUES(`always_show`),
`updater` = VALUES(`updater`),
`update_time` = VALUES(`update_time`),
`deleted` = VALUES(`deleted`);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(5067, '订刊窗口查询', 'subscription:window:query', 3, 1, 5066, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5068, '订刊窗口新增', 'subscription:window:create', 3, 2, 5066, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5069, '订刊窗口修改', 'subscription:window:update', 3, 3, 5066, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5070, '窗口刊物查询', 'subscription:window-spu:query', 3, 4, 5066, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5071, '窗口刊物新增', 'subscription:window-spu:create', 3, 5, 5066, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5072, '窗口刊物修改', 'subscription:window-spu:update', 3, 6, 5066, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5073, '窗口刊物删除', 'subscription:window-spu:delete', 3, 7, 5066, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5074, '窗口SKU查询', 'subscription:window-sku:query', 3, 8, 5066, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5075, '窗口SKU修改', 'subscription:window-sku:update', 3, 9, 5066, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5076, '特殊规则查询', 'subscription:window-spu-rule:query', 3, 10, 5066, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5077, '特殊规则新增', 'subscription:window-spu-rule:create', 3, 11, 5066, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5078, '特殊规则修改', 'subscription:window-spu-rule:update', 3, 12, 5066, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5079, '特殊规则删除', 'subscription:window-spu-rule:delete', 3, 13, 5066, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5080, '规则预览查询', 'subscription:preview:query', 3, 14, 5066, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5081, '规则模板', '', 2, 15, 5065, 'subscription-window-template', 'ep:files', 'subscription/window-template/index', 'SubscriptionWindowTemplate', 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5082, '规则模板查询', 'subscription:window-template:query', 3, 1, 5081, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5083, '规则模板新增', 'subscription:window-template:create', 3, 2, 5081, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5084, '规则模板修改', 'subscription:window-template:update', 3, 3, 5081, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5085, '规则模板删除', 'subscription:window-template:delete', 3, 4, 5081, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0')
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`permission` = VALUES(`permission`),
`type` = VALUES(`type`),
`sort` = VALUES(`sort`),
`parent_id` = VALUES(`parent_id`),
`path` = VALUES(`path`),
`icon` = VALUES(`icon`),
`component` = VALUES(`component`),
`component_name` = VALUES(`component_name`),
`status` = VALUES(`status`),
`visible` = VALUES(`visible`),
`keep_alive` = VALUES(`keep_alive`),
`always_show` = VALUES(`always_show`),
`updater` = VALUES(`updater`),
`update_time` = VALUES(`update_time`),
`deleted` = VALUES(`deleted`);

DELETE FROM `system_role_menu` WHERE `role_id` = 2 AND `menu_id` IN (5066, 5067, 5068, 5069, 5070, 5071, 5072, 5073, 5074, 5075, 5076, 5077, 5078, 5079, 5080, 5081, 5082, 5083, 5084, 5085);

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(2, 5066, '', NOW(), '', NOW(), b'0'),
(2, 5067, '', NOW(), '', NOW(), b'0'),
(2, 5068, '', NOW(), '', NOW(), b'0'),
(2, 5069, '', NOW(), '', NOW(), b'0'),
(2, 5070, '', NOW(), '', NOW(), b'0'),
(2, 5071, '', NOW(), '', NOW(), b'0'),
(2, 5072, '', NOW(), '', NOW(), b'0'),
(2, 5073, '', NOW(), '', NOW(), b'0'),
(2, 5074, '', NOW(), '', NOW(), b'0'),
(2, 5075, '', NOW(), '', NOW(), b'0'),
(2, 5076, '', NOW(), '', NOW(), b'0'),
(2, 5077, '', NOW(), '', NOW(), b'0'),
(2, 5078, '', NOW(), '', NOW(), b'0'),
(2, 5079, '', NOW(), '', NOW(), b'0'),
(2, 5080, '', NOW(), '', NOW(), b'0'),
(2, 5081, '', NOW(), '', NOW(), b'0'),
(2, 5082, '', NOW(), '', NOW(), b'0'),
(2, 5083, '', NOW(), '', NOW(), b'0'),
(2, 5084, '', NOW(), '', NOW(), b'0'),
(2, 5085, '', NOW(), '', NOW(), b'0');
