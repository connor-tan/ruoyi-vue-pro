-- 校刊汇：移除刊物销售周期 / 订刊窗口目标周期

DELIMITER //

DROP PROCEDURE IF EXISTS remove_publication_target_period//

CREATE PROCEDURE remove_publication_target_period()
BEGIN
  DECLARE v_rule_count bigint DEFAULT 0;

  IF EXISTS (
    SELECT 1 FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'subscription_rule_condition'
  ) THEN
    SELECT COUNT(1) INTO v_rule_count
    FROM subscription_rule_condition
    WHERE deleted = b'0'
      AND factor = 'SKU_TARGET_PERIOD';

    IF v_rule_count > 0 THEN
      SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '移除刊物周期失败：存在 SKU_TARGET_PERIOD 订刊规则条件，请先清理规则';
    END IF;
  END IF;

  SET @sql = IF(
    EXISTS (
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'subscription_window'
        AND INDEX_NAME = 'idx_subscription_window_year_period'
    ),
    'ALTER TABLE subscription_window DROP INDEX idx_subscription_window_year_period',
    'SELECT 1'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'subscription_window'
        AND COLUMN_NAME = 'target_period'
    ),
    'ALTER TABLE subscription_window DROP COLUMN target_period',
    'SELECT 1'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    NOT EXISTS (
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'subscription_window'
        AND INDEX_NAME = 'idx_subscription_window_year'
    ) AND EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'subscription_window'
        AND COLUMN_NAME = 'target_year_catalog_id'
    ),
    'ALTER TABLE subscription_window ADD INDEX idx_subscription_window_year (target_year_catalog_id)',
    'SELECT 1'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'product_publication_sku_ext'
        AND COLUMN_NAME = 'target_period'
    ),
    'ALTER TABLE product_publication_sku_ext DROP COLUMN target_period',
    'SELECT 1'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'trade_order_item'
        AND COLUMN_NAME = 'subscription_target_period'
    ),
    'ALTER TABLE trade_order_item DROP COLUMN subscription_target_period',
    'SELECT 1'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'trade_order_publication_issue'
        AND COLUMN_NAME = 'target_period'
    ),
    'ALTER TABLE trade_order_publication_issue DROP COLUMN target_period',
    'SELECT 1'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  SET @sql = IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'trade_publication_delivery_batch'
        AND COLUMN_NAME = 'target_period'
    ),
    'ALTER TABLE trade_publication_delivery_batch DROP COLUMN target_period',
    'SELECT 1'
  );
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
END//

CALL remove_publication_target_period()//

DROP PROCEDURE IF EXISTS remove_publication_target_period//

DELIMITER ;
