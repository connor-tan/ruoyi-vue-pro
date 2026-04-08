SET NAMES utf8mb4;

DELETE FROM `system_role_menu`
WHERE `menu_id` IN (5600, 5601, 5602, 5603, 5604, 5605, 5606, 5607, 5608, 5609, 5610, 5611, 5612, 5613, 5614, 5615, 5616, 5617, 5618);

DELETE FROM `system_menu`
WHERE `id` IN (5600, 5601, 5602, 5603, 5604, 5605, 5606, 5607, 5608, 5609, 5610, 5611, 5612, 5613, 5614, 5615, 5616, 5617, 5618);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(2014, '商品中心', '', 2, 1, 2000, 'center', 'ep:apple', 'mall/product/center/index', 'ProductCenter',
 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5604, '刊物基础资料', '', 2, 2, 2000, 'publication-meta', 'ep:collection-tag', 'mall/product/publication/meta/index', 'ProductPublicationMeta',
 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5609, '出版社', '', 2, 3, 2000, 'publication-publisher', 'ep:office-building', 'mall/product/publication/publisher/index', 'ProductPublicationPublisher',
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

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(2, 5601, '1', NOW(), '1', NOW(), b'0', 1),
(2, 5602, '1', NOW(), '1', NOW(), b'0', 1),
(2, 5603, '1', NOW(), '1', NOW(), b'0', 1),
(2, 5604, '1', NOW(), '1', NOW(), b'0', 1),
(2, 5605, '1', NOW(), '1', NOW(), b'0', 1),
(2, 5606, '1', NOW(), '1', NOW(), b'0', 1),
(2, 5607, '1', NOW(), '1', NOW(), b'0', 1),
(2, 5608, '1', NOW(), '1', NOW(), b'0', 1),
(2, 5609, '1', NOW(), '1', NOW(), b'0', 1),
(2, 5610, '1', NOW(), '1', NOW(), b'0', 1),
(2, 5611, '1', NOW(), '1', NOW(), b'0', 1),
(2, 5612, '1', NOW(), '1', NOW(), b'0', 1),
(2, 5613, '1', NOW(), '1', NOW(), b'0', 1),
(2, 5614, '1', NOW(), '1', NOW(), b'0', 1),
(2, 5615, '1', NOW(), '1', NOW(), b'0', 1),
(2, 5616, '1', NOW(), '1', NOW(), b'0', 1),
(2, 5617, '1', NOW(), '1', NOW(), b'0', 1),
(2, 5618, '1', NOW(), '1', NOW(), b'0', 1);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(5601, '刊物商品查询', 'product:publication-product:query', 3, 11, 2014, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5602, '刊物商品新增', 'product:publication-product:create', 3, 12, 2014, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5603, '刊物商品修改', 'product:publication-product:update', 3, 13, 2014, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5614, '刊物商品删除', 'product:publication-product:delete', 3, 14, 2014, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5605, '刊物类型查询', 'product:publication-type:query', 3, 1, 5604, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5606, '刊物类型新增', 'product:publication-type:create', 3, 2, 5604, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5607, '刊物类型修改', 'product:publication-type:update', 3, 3, 5604, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5608, '刊物类型删除', 'product:publication-type:delete', 3, 4, 5604, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5610, '出版社查询', 'product:publication-publisher:query', 3, 1, 5609, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5611, '出版社新增', 'product:publication-publisher:create', 3, 2, 5609, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5612, '出版社修改', 'product:publication-publisher:update', 3, 3, 5609, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5613, '出版社删除', 'product:publication-publisher:delete', 3, 4, 5609, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5615, '刊物主档查询', 'product:publication-title:query', 3, 5, 5604, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5616, '刊物主档新增', 'product:publication-title:create', 3, 6, 5604, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5617, '刊物主档修改', 'product:publication-title:update', 3, 7, 5604, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0'),
(5618, '刊物主档删除', 'product:publication-title:delete', 3, 8, 5604, '', '', NULL, NULL, 0, b'1', b'1', b'1', '', NOW(), '', NOW(), b'0')
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
