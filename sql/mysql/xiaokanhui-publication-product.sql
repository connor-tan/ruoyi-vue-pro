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
  issue_cycle varchar(64) NOT NULL COMMENT '出刊周期',
  issn varchar(64) DEFAULT NULL COMMENT 'ISSN',
  cn_code varchar(64) DEFAULT NULL COMMENT 'CN 刊号',
  post_distribution_code varchar(64) DEFAULT NULL COMMENT '邮发代号',
  fulfillment_mode varchar(64) NOT NULL DEFAULT 'SCHOOL_STATION' COMMENT '履约方式',
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
  target_period varchar(32) NOT NULL COMMENT '售卖周期',
  volume_label varchar(64) DEFAULT NULL COMMENT '册别',
  edition_label varchar(64) DEFAULT NULL COMMENT '版本',
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
