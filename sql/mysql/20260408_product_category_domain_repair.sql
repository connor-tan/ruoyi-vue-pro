SET NAMES utf8mb4;

-- 为普通商品补一棵最小可用分类树，避免与刊物商品分类树冲突
INSERT INTO `product_category`
(`id`, `parent_id`, `name`, `pic_url`, `sort`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(91011, 0, '普通商品', '', 95, 0, '', NOW(), '', NOW(), b'0', 0),
(91012, 91011, '通用商品', '', 10, 0, '', NOW(), '', NOW(), b'0', 0)
ON DUPLICATE KEY UPDATE
`parent_id` = VALUES(`parent_id`),
`name` = VALUES(`name`),
`pic_url` = VALUES(`pic_url`),
`sort` = VALUES(`sort`),
`status` = VALUES(`status`),
`updater` = VALUES(`updater`),
`update_time` = VALUES(`update_time`),
`deleted` = VALUES(`deleted`),
`tenant_id` = VALUES(`tenant_id`);

-- 将错误挂到刊物分类树下的普通商品迁回普通商品通用分类
UPDATE `product_spu`
SET `category_id` = 91012
WHERE `domain_type` = 'NORMAL'
  AND (`category_id` = 90001 OR `category_id` IN (90002, 90003, 90004, 90005, 90006, 90007, 90008, 90009, 90010));
