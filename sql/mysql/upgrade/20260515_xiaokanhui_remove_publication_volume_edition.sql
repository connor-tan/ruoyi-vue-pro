-- 校刊汇：干净去除刊物 SKU 册别 / 版本字段和规则因子

CREATE TEMPORARY TABLE IF NOT EXISTS tmp_xkh_remove_publication_sku_rule_ids (
  rule_id bigint PRIMARY KEY
) ENGINE = MEMORY;

TRUNCATE TABLE tmp_xkh_remove_publication_sku_rule_ids;

INSERT IGNORE INTO tmp_xkh_remove_publication_sku_rule_ids (rule_id)
SELECT DISTINCT rule_id
FROM subscription_rule_condition
WHERE factor IN ('SKU_VOLUME_LABEL', 'SKU_EDITION_LABEL');

DELETE condition_row
FROM subscription_rule_condition condition_row
JOIN tmp_xkh_remove_publication_sku_rule_ids deleted_rule ON deleted_rule.rule_id = condition_row.rule_id;

DELETE rule_row
FROM subscription_rule rule_row
JOIN tmp_xkh_remove_publication_sku_rule_ids deleted_rule ON deleted_rule.rule_id = rule_row.id;

DROP TEMPORARY TABLE IF EXISTS tmp_xkh_remove_publication_sku_rule_ids;

DELETE FROM system_dict_data
WHERE dict_type IN ('edu_publication_volume', 'edu_publication_edition');

DELETE FROM system_dict_type
WHERE type IN ('edu_publication_volume', 'edu_publication_edition');

SET @sql = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'product_publication_sku_ext'
      AND COLUMN_NAME = 'volume_label'
  ),
  'ALTER TABLE product_publication_sku_ext DROP COLUMN volume_label',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'product_publication_sku_ext'
      AND COLUMN_NAME = 'edition_label'
  ),
  'ALTER TABLE product_publication_sku_ext DROP COLUMN edition_label',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
