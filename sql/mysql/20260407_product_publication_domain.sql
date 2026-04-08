SET NAMES utf8mb4;

SET @product_spu_has_domain_type := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'product_spu'
      AND COLUMN_NAME = 'domain_type'
);
SET @product_spu_add_domain_type_sql := IF(
    @product_spu_has_domain_type = 0,
    'ALTER TABLE `product_spu` ADD COLUMN `domain_type` varchar(32) NOT NULL DEFAULT ''NORMAL'' COMMENT ''业务域类型'' AFTER `sort`',
    'SELECT 1'
);
PREPARE stmt FROM @product_spu_add_domain_type_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `product_spu`
SET `domain_type` = 'NORMAL'
WHERE `domain_type` IS NULL OR `domain_type` = '';

DELETE FROM `infra_config`
WHERE `config_key` = 'product.publication_root_category_id';

INSERT INTO `infra_config`
(`category`, `type`, `name`, `config_key`, `value`, `visible`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
('product', 2, '刊物商品根分类', 'product.publication_root_category_id', '90001', b'0', '刊物商品分类树根节点', '', NOW(), '', NOW(), b'0');

INSERT INTO `product_category`
(`id`, `parent_id`, `name`, `pic_url`, `sort`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(90001, 0, '刊物商品', '', 90, 0, '', NOW(), '', NOW(), b'0', 0),
(90002, 90001, '综合教辅', '', 10, 0, '', NOW(), '', NOW(), b'0', 0),
(90003, 90001, '语文教辅', '', 20, 0, '', NOW(), '', NOW(), b'0', 0),
(90004, 90001, '数学教辅', '', 30, 0, '', NOW(), '', NOW(), b'0', 0),
(90005, 90001, '英语教辅', '', 40, 0, '', NOW(), '', NOW(), b'0', 0),
(90006, 90001, '科普阅读', '', 50, 0, '', NOW(), '', NOW(), b'0', 0),
(90007, 90001, '文学阅读', '', 60, 0, '', NOW(), '', NOW(), b'0', 0),
(90008, 90001, '校园刊物', '', 70, 0, '', NOW(), '', NOW(), b'0', 0),
(90009, 90001, '主题套装', '', 80, 0, '', NOW(), '', NOW(), b'0', 0),
(90010, 90001, '工具资料', '', 90, 0, '', NOW(), '', NOW(), b'0', 0)
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

UPDATE `product_spu`
SET `category_id` = 90002
WHERE `domain_type` = 'PUBLICATION'
  AND (`category_id` IS NULL OR `category_id` = 90001 OR `category_id` NOT IN (90002, 90003, 90004, 90005, 90006, 90007, 90008, 90009, 90010));

CREATE TABLE IF NOT EXISTS `product_publication_type` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '刊物类型编号',
    `code` varchar(64) NOT NULL COMMENT '类型编码',
    `name` varchar(64) NOT NULL COMMENT '类型名称',
    `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
    `remark` varchar(255) NOT NULL DEFAULT '' COMMENT '备注',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_publication_type_code` (`code`),
    UNIQUE KEY `uk_product_publication_type_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='刊物类型';

CREATE TABLE IF NOT EXISTS `product_publication_publisher` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '出版社编号',
    `code` varchar(64) NOT NULL COMMENT '出版社编码',
    `name` varchar(128) NOT NULL COMMENT '出版社名称',
    `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
    `remark` varchar(255) NOT NULL DEFAULT '' COMMENT '备注',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_publication_publisher_code` (`code`),
    UNIQUE KEY `uk_product_publication_publisher_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出版社';

CREATE TABLE IF NOT EXISTS `product_publication_title` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '刊物主档编号',
    `code` varchar(64) NOT NULL COMMENT '刊物编码',
    `name` varchar(128) NOT NULL COMMENT '刊物名称',
    `type_id` bigint NOT NULL COMMENT '刊物类型编号',
    `publisher_id` bigint DEFAULT NULL COMMENT '出版社编号',
    `issue_cycle` varchar(32) NOT NULL DEFAULT '' COMMENT '出刊周期',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
    `remark` varchar(255) NOT NULL DEFAULT '' COMMENT '备注',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_publication_title_code` (`code`),
    UNIQUE KEY `uk_product_publication_title_name` (`name`),
    KEY `idx_product_publication_title_type_id` (`type_id`),
    KEY `idx_product_publication_title_publisher_id` (`publisher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='刊物主档';

CREATE TABLE IF NOT EXISTS `product_publication_title_identifier` (
    `publication_title_id` bigint NOT NULL COMMENT '刊物主档编号',
    `issn` varchar(64) NOT NULL DEFAULT '' COMMENT 'ISSN',
    `cn_code` varchar(64) NOT NULL DEFAULT '' COMMENT 'CN 编号',
    `post_distribution_code` varchar(64) NOT NULL DEFAULT '' COMMENT '邮发代号',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`publication_title_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='刊物主档标识信息';

CREATE TABLE IF NOT EXISTS `product_spu_publication` (
    `product_spu_id` bigint NOT NULL COMMENT '商品 SPU 编号',
    `publication_title_id` bigint NOT NULL COMMENT '刊物主档编号',
    `remark` varchar(255) NOT NULL DEFAULT '' COMMENT '备注',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`product_spu_id`),
    KEY `idx_product_spu_publication_title_id` (`publication_title_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品 SPU 与刊物主档关系';

CREATE TABLE IF NOT EXISTS `product_spu_grade` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `product_spu_id` bigint NOT NULL COMMENT '商品 SPU 编号',
    `grade_catalog_id` bigint NOT NULL COMMENT '标准年级编号',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_spu_grade_spu_grade` (`product_spu_id`, `grade_catalog_id`),
    KEY `idx_product_spu_grade_catalog_id` (`grade_catalog_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品 SPU 适用年级';

CREATE TABLE IF NOT EXISTS `product_sku_publication` (
    `product_sku_id` bigint NOT NULL COMMENT '商品 SKU 编号',
    `volume_label` varchar(64) NOT NULL DEFAULT '' COMMENT '册别',
    `edition_label` varchar(64) NOT NULL DEFAULT '' COMMENT '版本',
    `isbn` varchar(64) NOT NULL DEFAULT '' COMMENT 'ISBN',
    `remark` varchar(255) NOT NULL DEFAULT '' COMMENT '备注',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`product_sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品 SKU 刊物扩展';

INSERT INTO `product_publication_type`
(`id`, `code`, `name`, `sort`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(1, 'BOOK', '书本', 10, 0, '', '', NOW(), '', NOW(), b'0', 0),
(2, 'PERIODICAL', '期刊', 20, 0, '', '', NOW(), '', NOW(), b'0', 0),
(3, 'NEWSPAPER', '报纸', 30, 0, '', '', NOW(), '', NOW(), b'0', 0),
(4, 'PACKAGE', '套装', 40, 0, '', '', NOW(), '', NOW(), b'0', 0)
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`sort` = VALUES(`sort`),
`status` = VALUES(`status`),
`remark` = VALUES(`remark`),
`updater` = VALUES(`updater`),
`update_time` = VALUES(`update_time`),
`deleted` = VALUES(`deleted`),
`tenant_id` = VALUES(`tenant_id`);
