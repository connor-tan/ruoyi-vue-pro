SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `trade_order_delivery` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配送单编号',
  `order_id` bigint unsigned NOT NULL COMMENT '订单编号',
  `delivery_type` tinyint NOT NULL COMMENT '配送类型',
  `status` int NOT NULL COMMENT '配送状态',
  `product_count` int NOT NULL DEFAULT '0' COMMENT '商品数量',
  `pay_price` int NOT NULL DEFAULT '0' COMMENT '配送单实付金额，单位：分',
  `delivery_price` int NOT NULL DEFAULT '0' COMMENT '配送单运费，单位：分',
  `logistics_id` bigint DEFAULT NULL COMMENT '物流公司编号',
  `logistics_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '物流单号',
  `delivery_time` datetime DEFAULT NULL COMMENT '发货时间',
  `receive_time` datetime DEFAULT NULL COMMENT '收货时间',
  `receiver_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '收件人名称',
  `receiver_mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '收件人手机',
  `receiver_area_id` int DEFAULT NULL COMMENT '收件地区编号',
  `receiver_detail_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '收件详细地址',
  `school_id` bigint DEFAULT NULL COMMENT '学校编号',
  `school_name_snapshot` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '学校名称快照',
  `school_address_snapshot` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '学校地址快照',
  `warehouse_id` bigint DEFAULT NULL COMMENT '学校配送仓库编号',
  `warehouse_name_snapshot` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '学校配送仓库名称快照',
  `warehouse_address_snapshot` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '学校配送仓库地址快照',
  `contact_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '联系人',
  `contact_mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '联系电话',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_trade_order_delivery_order_id` (`order_id`),
  KEY `idx_trade_order_delivery_order_type` (`order_id`, `delivery_type`),
  KEY `idx_trade_order_delivery_warehouse_school` (`warehouse_id`, `school_id`),
  KEY `idx_trade_order_delivery_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='交易订单配送单';

ALTER TABLE `trade_order`
  MODIFY COLUMN `receiver_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '收件人名称',
  MODIFY COLUMN `receiver_mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '收件人手机';

SET @trade_order_item_add_delivery_id_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'trade_order_item'
        AND column_name = 'delivery_id'
    ),
    'SELECT 1',
    'ALTER TABLE `trade_order_item` ADD COLUMN `delivery_id` bigint DEFAULT NULL COMMENT ''配送单编号'' AFTER `order_id`'
  )
);
PREPARE trade_order_item_add_delivery_id_stmt FROM @trade_order_item_add_delivery_id_sql;
EXECUTE trade_order_item_add_delivery_id_stmt;
DEALLOCATE PREPARE trade_order_item_add_delivery_id_stmt;

SET @trade_order_item_add_student_name_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'trade_order_item'
        AND column_name = 'subscription_student_name_snapshot'
    ),
    'SELECT 1',
    'ALTER TABLE `trade_order_item` ADD COLUMN `subscription_student_name_snapshot` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT ''订刊学生名称快照'' AFTER `subscription_student_id`'
  )
);
PREPARE trade_order_item_add_student_name_stmt FROM @trade_order_item_add_student_name_sql;
EXECUTE trade_order_item_add_student_name_stmt;
DEALLOCATE PREPARE trade_order_item_add_student_name_stmt;

SET @trade_order_item_add_school_name_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'trade_order_item'
        AND column_name = 'subscription_school_name_snapshot'
    ),
    'SELECT 1',
    'ALTER TABLE `trade_order_item` ADD COLUMN `subscription_school_name_snapshot` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT ''订刊学校名称快照'' AFTER `subscription_school_id`'
  )
);
PREPARE trade_order_item_add_school_name_stmt FROM @trade_order_item_add_school_name_sql;
EXECUTE trade_order_item_add_school_name_stmt;
DEALLOCATE PREPARE trade_order_item_add_school_name_stmt;

SET @trade_order_item_add_grade_name_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'trade_order_item'
        AND column_name = 'subscription_grade_name_snapshot'
    ),
    'SELECT 1',
    'ALTER TABLE `trade_order_item` ADD COLUMN `subscription_grade_name_snapshot` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT ''订刊年级名称快照'' AFTER `subscription_grade_catalog_id`'
  )
);
PREPARE trade_order_item_add_grade_name_stmt FROM @trade_order_item_add_grade_name_sql;
EXECUTE trade_order_item_add_grade_name_stmt;
DEALLOCATE PREPARE trade_order_item_add_grade_name_stmt;

SET @trade_order_item_delivery_idx_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.statistics
      WHERE table_schema = DATABASE()
        AND table_name = 'trade_order_item'
        AND index_name = 'idx_trade_order_item_delivery_id'
    ),
    'SELECT 1',
    'ALTER TABLE `trade_order_item` ADD KEY `idx_trade_order_item_delivery_id` (`delivery_id`)'
  )
);
PREPARE trade_order_item_delivery_idx_stmt FROM @trade_order_item_delivery_idx_sql;
EXECUTE trade_order_item_delivery_idx_stmt;
DEALLOCATE PREPARE trade_order_item_delivery_idx_stmt;

INSERT INTO `system_dict_data`
(`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 3070, 3, '学校配送', '3', 'trade_delivery_type', 0, '', '', '', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_dict_data`
  WHERE `dict_type` = 'trade_delivery_type' AND `value` = '3' AND `deleted` = b'0'
);

UPDATE `system_dict_data`
SET `label` = '学校配送',
    `updater` = '1',
    `update_time` = NOW()
WHERE `dict_type` = 'trade_delivery_type'
  AND `value` = '3'
  AND `deleted` = b'0';

INSERT INTO `system_dict_data`
(`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 3071, 4, '混合配送', '4', 'trade_delivery_type', 0, '', '', '', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_dict_data`
  WHERE `dict_type` = 'trade_delivery_type' AND `value` = '4' AND `deleted` = b'0'
);
