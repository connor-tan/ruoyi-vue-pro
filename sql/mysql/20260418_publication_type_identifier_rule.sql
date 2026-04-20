SET NAMES utf8mb4;

SET @column_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'product_publication_type'
    AND COLUMN_NAME = 'identifier_rule'
);

SET @alter_sql := IF(
  @column_exists = 0,
  'ALTER TABLE `product_publication_type` ADD COLUMN `identifier_rule` varchar(64) NOT NULL DEFAULT ''NONE'' COMMENT ''标识规则：NONE/TITLE_PERIODICAL_IDENTIFIER_REQUIRED/SKU_ISBN_REQUIRED'' AFTER `name`',
  'SELECT 1'
);

PREPARE alter_stmt FROM @alter_sql;
EXECUTE alter_stmt;
DEALLOCATE PREPARE alter_stmt;

UPDATE `product_publication_type`
SET `identifier_rule` = CASE
  WHEN UPPER(`code`) = 'BOOK' THEN 'SKU_ISBN_REQUIRED'
  WHEN UPPER(`code`) IN ('PERIODICAL', 'NEWSPAPER') THEN 'TITLE_PERIODICAL_IDENTIFIER_REQUIRED'
  WHEN `identifier_rule` IS NULL OR `identifier_rule` = '' THEN 'NONE'
  ELSE `identifier_rule`
END
WHERE `deleted` = b'0'
  AND (
    `identifier_rule` IS NULL
    OR `identifier_rule` = ''
    OR UPPER(`code`) IN ('BOOK', 'PERIODICAL', 'NEWSPAPER')
  );
