-- 商品多分类完整重构：SPU 显式业务场景 + SPU 分类关系表

CREATE TABLE IF NOT EXISTS product_spu_category_rel (
    id bigint NOT NULL AUTO_INCREMENT COMMENT '关系编号',
    spu_id bigint NOT NULL COMMENT '商品 SPU 编号',
    category_id bigint NOT NULL COMMENT '商品分类编号',
    sort int NOT NULL DEFAULT 0 COMMENT '排序',
    creator varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
    create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
    update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    tenant_id bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (id),
    KEY idx_product_spu_category_rel_spu (spu_id),
    KEY idx_product_spu_category_rel_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品 SPU 分类关系';

SET @sql := IF(
    NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'product_spu' AND COLUMN_NAME = 'biz_scene'
    ),
    "ALTER TABLE product_spu ADD COLUMN biz_scene varchar(32) NOT NULL DEFAULT 'NORMAL' COMMENT '业务场景' AFTER description",
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'product_spu' AND COLUMN_NAME = 'category_id'
    ),
    "UPDATE product_spu spu INNER JOIN product_category category ON category.id = spu.category_id SET spu.biz_scene = category.biz_scene WHERE spu.category_id IS NOT NULL",
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'product_spu' AND COLUMN_NAME = 'category_id'
    ),
    "INSERT INTO product_spu_category_rel (spu_id, category_id, sort, creator, create_time, updater, update_time, deleted, tenant_id) SELECT spu.id, spu.category_id, 0, spu.creator, NOW(), spu.updater, NOW(), b'0', spu.tenant_id FROM product_spu spu WHERE spu.category_id IS NOT NULL AND spu.deleted = b'0' AND NOT EXISTS (SELECT 1 FROM product_spu_category_rel rel WHERE rel.spu_id = spu.id AND rel.category_id = spu.category_id AND rel.deleted = b'0')",
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'product_spu' AND COLUMN_NAME = 'category_id'
    ),
    'ALTER TABLE product_spu DROP COLUMN category_id',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_item' AND COLUMN_NAME = 'category_ids'
    ),
    "ALTER TABLE trade_order_item ADD COLUMN category_ids varchar(255) DEFAULT NULL COMMENT '商品分类编号快照' AFTER spu_name",
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order_item' AND COLUMN_NAME = 'category_names'
    ),
    "ALTER TABLE trade_order_item ADD COLUMN category_names varchar(1024) DEFAULT NULL COMMENT '商品分类名称快照' AFTER category_ids",
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
