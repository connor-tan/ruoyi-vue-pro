CREATE TABLE IF NOT EXISTS `product_spu` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `name` varchar(128) NOT NULL,
    `biz_scene` varchar(32) NOT NULL DEFAULT 'NORMAL',
    `pic_url` varchar(256),
    `price` int,
    `stock` int,
    `sort` int NOT NULL DEFAULT 0,
    `status` tinyint NOT NULL,
    `creator` varchar(64) DEFAULT '',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted` bit NOT NULL DEFAULT FALSE,
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `product_publication_spu_ext` (
    `spu_id` bigint NOT NULL,
    `publisher_id` bigint,
    `publication_type_id` bigint,
    `issue_mode` varchar(32) NOT NULL DEFAULT 'SINGLE',
    `issue_cycle` varchar(64),
    `creator` varchar(64) DEFAULT '',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted` bit NOT NULL DEFAULT FALSE,
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`spu_id`)
);

CREATE TABLE IF NOT EXISTS `product_spu_category_rel` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `spu_id` bigint NOT NULL,
    `category_id` bigint NOT NULL,
    `sort` int NOT NULL DEFAULT 0,
    `creator` varchar(64) DEFAULT '',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted` bit NOT NULL DEFAULT FALSE,
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `product_category` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `name` varchar(255) NOT NULL,
    `creator` varchar(64) DEFAULT '',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted` bit NOT NULL DEFAULT FALSE,
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `product_publisher` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `name` varchar(255) NOT NULL,
    `creator` varchar(64) DEFAULT '',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted` bit NOT NULL DEFAULT FALSE,
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `product_publication_type` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `name` varchar(255) NOT NULL,
    `creator` varchar(64) DEFAULT '',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted` bit NOT NULL DEFAULT FALSE,
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `product_sku` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `spu_id` bigint NOT NULL,
    `status` tinyint NOT NULL DEFAULT 0,
    `creator` varchar(64) DEFAULT '',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted` bit NOT NULL DEFAULT FALSE,
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `product_publication_sku_grade_rel` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `sku_id` bigint NOT NULL,
    `grade_catalog_id` bigint NOT NULL,
    `creator` varchar(64) DEFAULT '',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted` bit NOT NULL DEFAULT FALSE,
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `product_publication_sku_issue_template` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `sku_id` bigint NOT NULL,
    `issue_no` int NOT NULL,
    `issue_name` varchar(128) NOT NULL,
    `publish_offset_days` int DEFAULT NULL,
    `delivery_offset_days` int DEFAULT NULL,
    `sort` int NOT NULL DEFAULT 0,
    `status` tinyint NOT NULL DEFAULT 0,
    `remark` varchar(255),
    `creator` varchar(64) DEFAULT '',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted` bit NOT NULL DEFAULT FALSE,
    `active_issue_no` int GENERATED ALWAYS AS (CASE WHEN `deleted` = FALSE THEN `issue_no` ELSE NULL END),
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_publication_sku_issue_template_no` (`sku_id`, `active_issue_no`)
);

CREATE TABLE IF NOT EXISTS `subscription_window_offer` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `window_id` bigint NOT NULL,
    `product_spu_id` bigint NOT NULL,
    `recommend_flag` bit NOT NULL DEFAULT FALSE,
    `sort` int NOT NULL DEFAULT 0,
    `status` tinyint NOT NULL DEFAULT 0,
    `remark` varchar(255),
    `creator` varchar(64) DEFAULT '',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted` bit NOT NULL DEFAULT FALSE,
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `subscription_window` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `name` varchar(128) NOT NULL,
    `target_year_catalog_id` bigint,
    `target_year_name_snapshot` varchar(128),
    `target_year_start` int,
    `target_year_end` int,
    `start_time` timestamp,
    `end_time` timestamp,
    `grade_calc_rule` varchar(64),
    `grade_resolve_mode` varchar(64),
    `status` tinyint NOT NULL DEFAULT 0,
    `remark` varchar(255),
    `creator` varchar(64) DEFAULT '',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted` bit NOT NULL DEFAULT FALSE,
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `subscription_window_offer_sku` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `offer_id` bigint NOT NULL,
    `product_sku_id` bigint NOT NULL,
    `max_quantity_per_student` int NOT NULL DEFAULT 1,
    `sort` int NOT NULL DEFAULT 0,
    `status` tinyint NOT NULL DEFAULT 0,
    `remark` varchar(255),
    `creator` varchar(64) DEFAULT '',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted` bit NOT NULL DEFAULT FALSE,
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `subscription_offer_sku_issue` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `offer_id` bigint NOT NULL,
    `offer_sku_id` bigint NOT NULL,
    `issue_no` int NOT NULL,
    `issue_name` varchar(128) NOT NULL,
    `planned_publish_date` date DEFAULT NULL,
    `planned_delivery_date` date DEFAULT NULL,
    `sort` int NOT NULL DEFAULT 0,
    `status` tinyint NOT NULL DEFAULT 0,
    `remark` varchar(255),
    `creator` varchar(64) DEFAULT '',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted` bit NOT NULL DEFAULT FALSE,
    `active_issue_no` int GENERATED ALWAYS AS (CASE WHEN `deleted` = FALSE THEN `issue_no` ELSE NULL END),
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_subscription_offer_sku_issue_no` (`offer_sku_id`, `active_issue_no`)
);
