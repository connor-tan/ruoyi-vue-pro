SET NAMES utf8mb4;

DELETE FROM `system_role_menu` WHERE `menu_id` IN (5070, 5071, 5072);
DELETE FROM `system_menu` WHERE `id` IN (5081, 5082);
DELETE FROM `system_menu` WHERE `id` IN (5070, 5071, 5072);

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
(5073, '窗口刊物查询', 'subscription:window-publication:query', 3, 4, 5066, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5074, '窗口刊物新增', 'subscription:window-publication:create', 3, 5, 5066, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5075, '窗口刊物修改', 'subscription:window-publication:update', 3, 6, 5066, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5076, '可见规则查询', 'subscription:window-publication-rule:query', 3, 7, 5066, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5077, '可见规则新增', 'subscription:window-publication-rule:create', 3, 8, 5066, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5078, '可见规则修改', 'subscription:window-publication-rule:update', 3, 9, 5066, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5079, '规则预览查询', 'subscription:preview:query', 3, 10, 5066, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5080, '订刊支持查询', 'subscription:support:query', 3, 11, 5066, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0')
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
