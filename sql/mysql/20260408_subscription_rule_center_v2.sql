SET NAMES utf8mb4;

ALTER TABLE `sub_window`
  ADD COLUMN `target_year_start` int DEFAULT NULL COMMENT '目标学年开始年份' AFTER `end_time`,
  ADD COLUMN `target_year_end` int DEFAULT NULL COMMENT '目标学年结束年份' AFTER `target_year_start`;

UPDATE `sub_window` w
JOIN `edu_school_year` sy ON sy.id = w.target_school_year_id
SET w.target_year_start = sy.year_start,
    w.target_year_end = sy.year_end
WHERE w.target_year_start IS NULL
   OR w.target_year_end IS NULL;

ALTER TABLE `sub_window`
  MODIFY COLUMN `target_year_start` int NOT NULL COMMENT '目标学年开始年份',
  MODIFY COLUMN `target_year_end` int NOT NULL COMMENT '目标学年结束年份';

ALTER TABLE `sub_window`
  DROP COLUMN `target_school_year_id`;

CREATE TABLE IF NOT EXISTS `sub_window_spu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `window_id` bigint NOT NULL COMMENT '窗口编号',
  `product_spu_id` bigint NOT NULL COMMENT '刊物商品 SPU 编号',
  `recommend_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '推荐标记',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sub_window_spu_window_product` (`window_id`,`product_spu_id`,`deleted`),
  KEY `idx_sub_window_spu_window` (`window_id`)
) COMMENT='订刊窗口刊物 SPU';

CREATE TABLE IF NOT EXISTS `sub_window_spu_grade` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `window_spu_id` bigint NOT NULL COMMENT '窗口刊物编号',
  `grade_catalog_id` bigint NOT NULL COMMENT '基础可见年级编号',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sub_window_spu_grade` (`window_spu_id`,`grade_catalog_id`,`deleted`),
  KEY `idx_sub_window_spu_grade_catalog` (`grade_catalog_id`)
) COMMENT='订刊窗口刊物基础可见年级';

CREATE TABLE IF NOT EXISTS `sub_window_spu_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `window_spu_id` bigint NOT NULL COMMENT '窗口刊物编号',
  `effect_type` varchar(32) NOT NULL COMMENT '规则效果',
  `scope_type` varchar(32) NOT NULL COMMENT '规则范围',
  `school_id` bigint DEFAULT NULL COMMENT '学校编号',
  `grade_catalog_id` bigint DEFAULT NULL COMMENT '年级编号',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_sub_window_spu_rule_window_spu` (`window_spu_id`),
  KEY `idx_sub_window_spu_rule_scope` (`scope_type`,`school_id`,`grade_catalog_id`)
) COMMENT='订刊窗口刊物特殊规则';

CREATE TABLE IF NOT EXISTS `sub_window_sku` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `window_spu_id` bigint NOT NULL COMMENT '窗口刊物编号',
  `product_sku_id` bigint NOT NULL COMMENT '商品 SKU 编号',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `max_quantity_per_student` int NOT NULL DEFAULT '1' COMMENT '每生限购',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sub_window_sku_window_spu_product_sku` (`window_spu_id`,`product_sku_id`,`deleted`),
  KEY `idx_sub_window_sku_status` (`status`)
) COMMENT='订刊窗口可售 SKU';
