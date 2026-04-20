SET NAMES utf8mb4;

ALTER TABLE `product_sku_publication`
  ADD COLUMN `target_period` varchar(32) NOT NULL DEFAULT 'FULL_YEAR' COMMENT '适用周期：FIRST_TERM/SECOND_TERM/FULL_YEAR' AFTER `edition_label`;

UPDATE `product_sku_publication`
SET `target_period` = CASE
  WHEN `volume_label` IN ('上', '上册', '上学期')
    OR `volume_label` LIKE '%上册%'
    OR `volume_label` LIKE '%上学期%' THEN 'FIRST_TERM'
  WHEN `volume_label` IN ('下', '下册', '下学期')
    OR `volume_label` LIKE '%下册%'
    OR `volume_label` LIKE '%下学期%' THEN 'SECOND_TERM'
  ELSE 'FULL_YEAR'
END
WHERE `target_period` IS NULL
   OR `target_period` = ''
   OR `target_period` = 'FULL_YEAR';
