SET NAMES utf8mb4;

CREATE TABLE `sub_window` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(128) NOT NULL COMMENT '窗口名称',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `target_school_year_id` bigint NOT NULL COMMENT '目标学年ID',
  `target_semester` tinyint NOT NULL COMMENT '目标学期',
  `grade_calc_rule` varchar(32) NOT NULL COMMENT '年级计算规则',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_sub_window_status_time` (`status`,`start_time`,`end_time`)
) COMMENT='订刊窗口';

CREATE TABLE `sub_window_publication` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `window_id` bigint NOT NULL COMMENT '窗口ID',
  `product_spu_id` bigint NOT NULL COMMENT '商品SPU ID',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `recommend_flag` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否推荐',
  `max_quantity_per_student` int NOT NULL DEFAULT '1' COMMENT '每个学生最大订购数量',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sub_window_publication_relation` (`window_id`,`product_spu_id`),
  KEY `idx_sub_window_publication_window_status_sort` (`window_id`,`status`,`sort`)
) COMMENT='窗口刊物开放关系';

CREATE TABLE `sub_window_publication_grade` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `window_publication_id` bigint NOT NULL COMMENT '窗口刊物ID',
  `grade_catalog_id` bigint NOT NULL COMMENT '全局年级目录ID',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sub_window_publication_grade_relation` (`window_publication_id`,`grade_catalog_id`)
) COMMENT='窗口刊物基础可见年级';

CREATE TABLE `sub_window_publication_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `window_publication_id` bigint NOT NULL COMMENT '窗口刊物ID',
  `effect_type` varchar(16) NOT NULL COMMENT '规则效果',
  `scope_type` varchar(32) NOT NULL COMMENT '规则范围',
  `school_id` bigint DEFAULT NULL COMMENT '学校ID',
  `grade_catalog_id` bigint DEFAULT NULL COMMENT '全局年级目录ID',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_sub_window_publication_rule_window_publication_id` (`window_publication_id`)
) COMMENT='窗口刊物特殊可见规则';
