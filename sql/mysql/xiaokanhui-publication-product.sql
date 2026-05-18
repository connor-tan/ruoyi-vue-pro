-- xiaokanhui 统一商品中心 / 刊物商品改造

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'product_category' AND COLUMN_NAME = 'biz_scene'
  ),
  'SELECT 1',
  "ALTER TABLE product_category ADD COLUMN biz_scene varchar(32) NOT NULL DEFAULT 'NORMAL' COMMENT '业务场景' AFTER status"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- subscription 规则中心新模型（offer / offerSku）

DROP TABLE IF EXISTS sub_window_rule_condition;
DROP TABLE IF EXISTS sub_window_rule;
DROP TABLE IF EXISTS sub_window_sku;
DROP TABLE IF EXISTS sub_window_spu_grade;
DROP TABLE IF EXISTS sub_window_spu_rule;
DROP TABLE IF EXISTS sub_window_spu;
DROP TABLE IF EXISTS sub_window_template;
DROP TABLE IF EXISTS sub_window;
DROP TABLE IF EXISTS subscription_window_spu_rule;
DROP TABLE IF EXISTS subscription_window_spu_grade;
DROP TABLE IF EXISTS subscription_window_spu;
DROP TABLE IF EXISTS subscription_window_sku;

CREATE TABLE IF NOT EXISTS subscription_window (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  name varchar(128) NOT NULL COMMENT '窗口名称',
  target_year_catalog_id bigint NOT NULL COMMENT '目标学年目录编号',
  target_year_name_snapshot varchar(64) DEFAULT NULL COMMENT '目标学年名称快照',
  target_year_start int NOT NULL COMMENT '目标学年开始年份',
  target_year_end int NOT NULL COMMENT '目标学年结束年份',
  start_time datetime NOT NULL COMMENT '开始时间',
  end_time datetime NOT NULL COMMENT '结束时间',
  grade_calc_rule varchar(32) NOT NULL COMMENT '年级计算规则',
  grade_resolve_mode varchar(32) NOT NULL COMMENT '年级解析模式',
  status tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  remark varchar(255) DEFAULT NULL COMMENT '备注',
  creator varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  tenant_id bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (id),
  KEY idx_subscription_window_open (status, start_time, end_time),
  KEY idx_subscription_window_year (target_year_catalog_id)
) COMMENT='订刊窗口';

CREATE TABLE IF NOT EXISTS subscription_window_offer (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  window_id bigint NOT NULL COMMENT '订刊窗口编号',
  product_spu_id bigint NOT NULL COMMENT '刊物商品 SPU 编号',
  recommend_flag bit(1) NOT NULL DEFAULT b'0' COMMENT '是否推荐',
  sort int NOT NULL DEFAULT 0 COMMENT '排序',
  status tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  remark varchar(255) DEFAULT NULL COMMENT '备注',
  creator varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  tenant_id bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  active_product_spu_id bigint GENERATED ALWAYS AS (IF(deleted = b'0', product_spu_id, NULL)) STORED COMMENT '未删除刊物商品 SPU 唯一键辅助列',
  PRIMARY KEY (id),
  UNIQUE KEY uk_subscription_offer_product (tenant_id, window_id, active_product_spu_id),
  KEY idx_subscription_offer_window (window_id, status, sort)
) COMMENT='订刊窗口刊物';

CREATE TABLE IF NOT EXISTS subscription_window_offer_sku (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  offer_id bigint NOT NULL COMMENT '窗口刊物编号',
  product_sku_id bigint NOT NULL COMMENT '商品 SKU 编号',
  sort int NOT NULL DEFAULT 0 COMMENT '排序',
  status tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  max_quantity_per_student int NOT NULL DEFAULT 1 COMMENT '每学生限购数量',
  remark varchar(255) DEFAULT NULL COMMENT '备注',
  creator varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  tenant_id bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  active_product_sku_id bigint GENERATED ALWAYS AS (IF(deleted = b'0', product_sku_id, NULL)) STORED COMMENT '未删除商品 SKU 唯一键辅助列',
  PRIMARY KEY (id),
  UNIQUE KEY uk_subscription_offer_sku_product (tenant_id, offer_id, active_product_sku_id),
  KEY idx_subscription_offer_sku_offer (offer_id, status, sort),
  KEY idx_subscription_offer_sku_product (product_sku_id)
) COMMENT='订刊窗口刊物 SKU';

CREATE TABLE IF NOT EXISTS subscription_window_offer_grade_rel (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  offer_id bigint NOT NULL COMMENT '窗口刊物编号',
  grade_catalog_id bigint NOT NULL COMMENT '年级目录编号',
  creator varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  tenant_id bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  active_grade_catalog_id bigint GENERATED ALWAYS AS (IF(deleted = b'0', grade_catalog_id, NULL)) STORED COMMENT '未删除年级目录唯一键辅助列',
  PRIMARY KEY (id),
  UNIQUE KEY uk_subscription_offer_grade (tenant_id, offer_id, active_grade_catalog_id),
  KEY idx_subscription_offer_grade_grade (grade_catalog_id)
) COMMENT='订刊窗口刊物年级收窄';

-- 订刊窗口刊物软删除唯一键修正：只约束未删除记录，允许同一窗口刊物多次移除后保留历史记录
SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'subscription_window_offer' AND COLUMN_NAME = 'active_product_spu_id'
  ),
  'SELECT 1',
  "ALTER TABLE subscription_window_offer ADD COLUMN active_product_spu_id bigint GENERATED ALWAYS AS (IF(deleted = b'0', product_spu_id, NULL)) STORED COMMENT '未删除刊物商品 SPU 唯一键辅助列' AFTER tenant_id"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'subscription_window_offer' AND INDEX_NAME = 'uk_subscription_offer_product'
  ),
  'ALTER TABLE subscription_window_offer DROP INDEX uk_subscription_offer_product',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

ALTER TABLE subscription_window_offer
  ADD UNIQUE KEY uk_subscription_offer_product (tenant_id, window_id, active_product_spu_id);

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'subscription_window_offer_sku' AND COLUMN_NAME = 'active_product_sku_id'
  ),
  'SELECT 1',
  "ALTER TABLE subscription_window_offer_sku ADD COLUMN active_product_sku_id bigint GENERATED ALWAYS AS (IF(deleted = b'0', product_sku_id, NULL)) STORED COMMENT '未删除商品 SKU 唯一键辅助列' AFTER tenant_id"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'subscription_window_offer_sku' AND INDEX_NAME = 'uk_subscription_offer_sku_product'
  ),
  'ALTER TABLE subscription_window_offer_sku DROP INDEX uk_subscription_offer_sku_product',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

ALTER TABLE subscription_window_offer_sku
  ADD UNIQUE KEY uk_subscription_offer_sku_product (tenant_id, offer_id, active_product_sku_id);

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'subscription_window_offer_grade_rel' AND COLUMN_NAME = 'active_grade_catalog_id'
  ),
  'SELECT 1',
  "ALTER TABLE subscription_window_offer_grade_rel ADD COLUMN active_grade_catalog_id bigint GENERATED ALWAYS AS (IF(deleted = b'0', grade_catalog_id, NULL)) STORED COMMENT '未删除年级目录唯一键辅助列' AFTER tenant_id"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'subscription_window_offer_grade_rel' AND INDEX_NAME = 'uk_subscription_offer_grade'
  ),
  'ALTER TABLE subscription_window_offer_grade_rel DROP INDEX uk_subscription_offer_grade',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

ALTER TABLE subscription_window_offer_grade_rel
  ADD UNIQUE KEY uk_subscription_offer_grade (tenant_id, offer_id, active_grade_catalog_id);

CREATE TABLE IF NOT EXISTS subscription_rule (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  window_id bigint NOT NULL COMMENT '订刊窗口编号',
  offer_id bigint DEFAULT NULL COMMENT '窗口刊物编号；为空表示窗口级规则',
  name varchar(128) NOT NULL COMMENT '规则名称',
  effect_type varchar(32) NOT NULL COMMENT '规则作用：INCLUDE/EXCLUDE',
  allow_grade_override bit(1) NOT NULL DEFAULT b'0' COMMENT '是否允许突破 SKU 年级',
  status tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  remark varchar(255) DEFAULT NULL COMMENT '备注',
  creator varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  tenant_id bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (id),
  KEY idx_subscription_rule_window (window_id, status),
  KEY idx_subscription_rule_offer (offer_id, status)
) COMMENT='订刊特殊规则';

CREATE TABLE IF NOT EXISTS subscription_rule_condition (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  rule_id bigint NOT NULL COMMENT '规则编号',
  factor varchar(64) NOT NULL COMMENT '规则因子',
  operator varchar(32) NOT NULL DEFAULT 'EQ' COMMENT '操作符',
  value varchar(128) NOT NULL COMMENT '因子值',
  value_name varchar(128) DEFAULT NULL COMMENT '因子展示值',
  creator varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  tenant_id bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (id),
  KEY idx_subscription_rule_condition_rule (rule_id)
) COMMENT='订刊特殊规则条件';

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_cart' AND COLUMN_NAME = 'subscription_offer_sku_id'
  ),
  'SELECT 1',
  "ALTER TABLE trade_cart ADD COLUMN subscription_offer_sku_id bigint DEFAULT NULL COMMENT '订刊窗口 SKU 编号（offerSku）' AFTER subscription_student_id"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_cart' AND INDEX_NAME = 'idx_trade_cart_subscription_offer_sku'
  ),
  'SELECT 1',
  'ALTER TABLE trade_cart ADD INDEX idx_trade_cart_subscription_offer_sku (user_id, subscription_student_id, subscription_offer_sku_id)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_item' AND COLUMN_NAME = 'subscription_offer_id'
  ),
  'SELECT 1',
  "ALTER TABLE trade_order_item ADD COLUMN subscription_offer_id bigint DEFAULT NULL COMMENT '订刊窗口刊物编号（offer）' AFTER subscription_student_id"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_item' AND COLUMN_NAME = 'subscription_offer_sku_id'
  ),
  'SELECT 1',
  "ALTER TABLE trade_order_item ADD COLUMN subscription_offer_sku_id bigint DEFAULT NULL COMMENT '订刊窗口 SKU 编号（offerSku）' AFTER subscription_offer_id"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_cart' AND COLUMN_NAME = 'subscription_window_sku_id'
  ),
  'ALTER TABLE trade_cart DROP COLUMN subscription_window_sku_id',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_item' AND COLUMN_NAME = 'subscription_window_spu_id'
  ),
  'ALTER TABLE trade_order_item DROP COLUMN subscription_window_spu_id',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_item' AND COLUMN_NAME = 'subscription_window_sku_id'
  ),
  'ALTER TABLE trade_order_item DROP COLUMN subscription_window_sku_id',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_item' AND INDEX_NAME = 'idx_trade_order_item_subscription_offer_sku'
  ),
  'SELECT 1',
  'ALTER TABLE trade_order_item ADD INDEX idx_trade_order_item_subscription_offer_sku (user_id, subscription_student_id, subscription_offer_sku_id)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'product_sku' AND COLUMN_NAME = 'name'
  ),
  'SELECT 1',
  "ALTER TABLE product_sku ADD COLUMN name varchar(128) NOT NULL DEFAULT '' COMMENT 'SKU 名称' AFTER spu_id"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'product_sku' AND COLUMN_NAME = 'status'
  ),
  'SELECT 1',
  "ALTER TABLE product_sku ADD COLUMN status tinyint NOT NULL DEFAULT 0 COMMENT 'SKU 状态' AFTER sales_count"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'product_spu' AND COLUMN_NAME = 'delivery_types'
  ),
  'SELECT 1',
  "ALTER TABLE product_spu ADD COLUMN delivery_types varchar(255) DEFAULT NULL COMMENT '配送方式数组' AFTER stock"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'product_spu' AND COLUMN_NAME = 'delivery_template_id'
      AND IS_NULLABLE = 'YES'
  ),
  'SELECT 1',
  "ALTER TABLE product_spu MODIFY COLUMN delivery_template_id bigint NULL COMMENT '物流配置模板编号'"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE product_category
SET biz_scene = 'PUBLICATION'
WHERE id IN (90001, 90002, 90003, 90004, 90005, 90006, 90007, 90008, 90009, 90010);

UPDATE system_menu
SET name = '刊物类型',
    path = 'publication-type',
    component = 'edu/publication/type/index',
    icon = 'ep:collection-tag'
WHERE id = 5604;

UPDATE system_menu
SET parent_id = 5065
WHERE id IN (5604, 5609);

UPDATE system_menu
SET component = 'edu/publication/publisher/index'
WHERE id = 5609;

UPDATE system_menu
SET permission = CASE id
    WHEN 5605 THEN 'edu:publication-type:query'
    WHEN 5606 THEN 'edu:publication-type:create'
    WHEN 5607 THEN 'edu:publication-type:update'
    WHEN 5608 THEN 'edu:publication-type:delete'
    WHEN 5610 THEN 'edu:publication-publisher:query'
    WHEN 5611 THEN 'edu:publication-publisher:create'
    WHEN 5612 THEN 'edu:publication-publisher:update'
    WHEN 5613 THEN 'edu:publication-publisher:delete'
    ELSE permission
END
WHERE id IN (5605, 5606, 5607, 5608, 5610, 5611, 5612, 5613);

UPDATE system_menu
SET status = 1
WHERE id IN (5615, 5616, 5617, 5618);

CREATE TABLE IF NOT EXISTS product_publisher (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '出版社编号',
  name varchar(255) NOT NULL COMMENT '出版社名称',
  sort int NOT NULL DEFAULT 0 COMMENT '排序',
  status tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  remark varchar(255) DEFAULT NULL COMMENT '备注',
  creator varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  tenant_id bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (id),
  UNIQUE KEY uk_product_publisher_name (name)
) COMMENT='出版社';

CREATE TABLE IF NOT EXISTS product_publication_type (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '刊物类型编号',
  name varchar(255) NOT NULL COMMENT '刊物类型名称',
  identifier_rule varchar(64) NOT NULL DEFAULT 'NONE' COMMENT '标识规则',
  sort int NOT NULL DEFAULT 0 COMMENT '排序',
  status tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  remark varchar(255) DEFAULT NULL COMMENT '备注',
  creator varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  tenant_id bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (id),
  UNIQUE KEY uk_product_publication_type_name (name)
) COMMENT='刊物类型';

CREATE TABLE IF NOT EXISTS product_publication_spu_ext (
  spu_id bigint NOT NULL COMMENT 'SPU 编号',
  publisher_id bigint NOT NULL COMMENT '出版社编号',
  publication_type_id bigint NOT NULL COMMENT '刊物类型编号',
  issue_mode varchar(32) NOT NULL DEFAULT 'SINGLE' COMMENT '期次模式：PERIODICAL 期刊，SINGLE 独立刊物',
  issue_cycle varchar(64) NOT NULL COMMENT '出刊周期',
  issn varchar(64) DEFAULT NULL COMMENT 'ISSN',
  cn_code varchar(64) DEFAULT NULL COMMENT 'CN 刊号',
  post_distribution_code varchar(64) DEFAULT NULL COMMENT '邮发代号',
  creator varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  tenant_id bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (spu_id)
) COMMENT='刊物 SPU 扩展';

CREATE TABLE IF NOT EXISTS product_publication_sku_ext (
  sku_id bigint NOT NULL COMMENT 'SKU 编号',
  isbn varchar(64) DEFAULT NULL COMMENT 'ISBN',
  remark varchar(255) DEFAULT NULL COMMENT '备注',
  creator varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  tenant_id bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (sku_id)
) COMMENT='刊物 SKU 扩展';

CREATE TABLE IF NOT EXISTS product_publication_sku_issue_template (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '默认期次模板编号',
  sku_id bigint NOT NULL COMMENT '商品 SKU 编号',
  issue_no int NOT NULL COMMENT '期号',
  issue_name varchar(128) NOT NULL COMMENT '期次名称',
  publish_offset_days int DEFAULT NULL COMMENT '计划出刊日期相对订刊窗口开始日期的偏移天数',
  delivery_offset_days int DEFAULT NULL COMMENT '计划配送日期相对订刊窗口开始日期的偏移天数',
  sort int NOT NULL DEFAULT 0 COMMENT '排序',
  status tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  remark varchar(255) DEFAULT NULL COMMENT '备注',
  creator varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  tenant_id bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  active_issue_no int GENERATED ALWAYS AS (CASE WHEN deleted = b'0' THEN issue_no ELSE NULL END) STORED COMMENT '启用唯一期号',
  PRIMARY KEY (id),
  UNIQUE KEY uk_product_publication_sku_issue_template_no (sku_id, active_issue_no),
  KEY idx_product_publication_sku_issue_template_sku (sku_id, status, sort)
) COMMENT='刊物 SKU 默认期次模板';

CREATE TABLE IF NOT EXISTS product_publication_sku_grade_rel (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  sku_id bigint NOT NULL COMMENT 'SKU 编号',
  grade_catalog_id bigint NOT NULL COMMENT '年级目录编号',
  creator varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  tenant_id bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (id),
  UNIQUE KEY uk_product_publication_sku_grade (sku_id, grade_catalog_id),
  KEY idx_product_publication_grade_catalog (grade_catalog_id)
) COMMENT='刊物 SKU 适用年级';

DROP TABLE IF EXISTS product_spu_grade;
DROP TABLE IF EXISTS product_publication_title;
DROP TABLE IF EXISTS subscription_window_spu_rule;
DROP TABLE IF EXISTS subscription_window_spu_grade;
DROP TABLE IF EXISTS subscription_window_spu;
DROP TABLE IF EXISTS subscription_window_sku;

-- trade 配送组改造

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_delivery' AND COLUMN_NAME = 'pick_up_store_id'
  ),
  'SELECT 1',
  "ALTER TABLE trade_order_delivery ADD COLUMN pick_up_store_id bigint DEFAULT NULL COMMENT '自提门店编号' AFTER receiver_detail_address"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_delivery' AND COLUMN_NAME = 'pick_up_verify_code'
  ),
  'SELECT 1',
  "ALTER TABLE trade_order_delivery ADD COLUMN pick_up_verify_code varchar(32) DEFAULT NULL COMMENT '自提核销码' AFTER pick_up_store_id"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_delivery' AND INDEX_NAME = 'uk_trade_order_delivery_pick_up_verify_code'
  ),
  'SELECT 1',
  "ALTER TABLE trade_order_delivery ADD UNIQUE KEY uk_trade_order_delivery_pick_up_verify_code (pick_up_verify_code)"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_delivery' AND COLUMN_NAME = 'biz_scene'
  ),
  'SELECT 1',
  "ALTER TABLE trade_order_delivery ADD COLUMN biz_scene varchar(32) DEFAULT NULL COMMENT '业务场景' AFTER contact_mobile"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_delivery' AND COLUMN_NAME = 'student_id'
  ),
  'SELECT 1',
  "ALTER TABLE trade_order_delivery ADD COLUMN student_id bigint DEFAULT NULL COMMENT '订刊学生编号' AFTER biz_scene"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_delivery' AND COLUMN_NAME = 'student_name_snapshot'
  ),
  'SELECT 1',
  "ALTER TABLE trade_order_delivery ADD COLUMN student_name_snapshot varchar(64) DEFAULT NULL COMMENT '订刊学生名称快照' AFTER student_id"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_delivery' AND COLUMN_NAME = 'class_id'
  ),
  'SELECT 1',
  "ALTER TABLE trade_order_delivery ADD COLUMN class_id bigint DEFAULT NULL COMMENT '订刊班级编号' AFTER student_name_snapshot"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_delivery' AND COLUMN_NAME = 'class_name_snapshot'
  ),
  'SELECT 1',
  "ALTER TABLE trade_order_delivery ADD COLUMN class_name_snapshot varchar(128) DEFAULT NULL COMMENT '订刊班级名称快照' AFTER class_id"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_delivery' AND COLUMN_NAME = 'grade_catalog_id'
  ),
  'SELECT 1',
  "ALTER TABLE trade_order_delivery ADD COLUMN grade_catalog_id bigint DEFAULT NULL COMMENT '订刊年级目录编号' AFTER class_name_snapshot"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_delivery' AND COLUMN_NAME = 'grade_name_snapshot'
  ),
  'SELECT 1',
  "ALTER TABLE trade_order_delivery ADD COLUMN grade_name_snapshot varchar(64) DEFAULT NULL COMMENT '订刊年级名称快照' AFTER grade_catalog_id"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
