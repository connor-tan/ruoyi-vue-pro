-- 刊物期次批次审查修复：批次明细必须绑定订单期次。
-- 当前项目不保留旧刊物批次模型兼容，order_issue_id 为空的旧批次明细按冲突数据清理。

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_publication_delivery_batch_item' AND COLUMN_NAME = 'order_issue_id'
  ),
  'SELECT 1',
  "ALTER TABLE trade_publication_delivery_batch_item ADD COLUMN order_issue_id bigint DEFAULT NULL COMMENT '订单刊物期次编号' AFTER order_item_id"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

DELETE FROM trade_publication_delivery_batch_item WHERE order_issue_id IS NULL;

DELETE b
FROM trade_publication_delivery_batch b
LEFT JOIN trade_publication_delivery_batch_item bi ON bi.batch_id = b.id
WHERE bi.id IS NULL;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'trade_publication_delivery_batch_item'
      AND COLUMN_NAME = 'order_issue_id'
      AND IS_NULLABLE = 'YES'
  ),
  "ALTER TABLE trade_publication_delivery_batch_item MODIFY order_issue_id bigint NOT NULL COMMENT '订单刊物期次编号'",
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
