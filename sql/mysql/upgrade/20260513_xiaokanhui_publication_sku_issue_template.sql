-- 校刊汇：刊物 SKU 默认期次模板

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
  active_issue_no int GENERATED ALWAYS AS (CASE WHEN deleted = b'0' THEN issue_no ELSE NULL END) STORED COMMENT '启用唯一期号',
  PRIMARY KEY (id),
  UNIQUE KEY uk_product_publication_sku_issue_template_no (sku_id, active_issue_no),
  KEY idx_product_publication_sku_issue_template_sku (sku_id, status, sort)
) COMMENT='刊物 SKU 默认期次模板';
