SET NAMES utf8mb4;

ALTER TABLE `sub_window_template`
  ADD COLUMN `grade_resolve_mode` varchar(32) NOT NULL DEFAULT 'CURRENT_CHAIN' COMMENT '年级解析模式' AFTER `grade_calc_rule`;

UPDATE `sub_window_template`
SET `grade_resolve_mode` = CASE
  WHEN `code` = 'NEW_YEAR_PRE_SALE' THEN 'TARGET_CLASS_FIRST'
  ELSE 'CURRENT_CHAIN'
END
WHERE `grade_resolve_mode` IS NULL
   OR `grade_resolve_mode` = ''
   OR `grade_resolve_mode` = 'CURRENT_CHAIN';

ALTER TABLE `sub_window`
  ADD COLUMN `grade_resolve_mode` varchar(32) NOT NULL DEFAULT 'CURRENT_CHAIN' COMMENT '年级解析模式' AFTER `grade_calc_rule`;

UPDATE `sub_window` w
LEFT JOIN `sub_window_template` t ON t.id = w.template_id AND t.deleted = b'0'
SET w.`grade_resolve_mode` = CASE
  WHEN t.code = 'NEW_YEAR_PRE_SALE' THEN 'TARGET_CLASS_FIRST'
  ELSE 'CURRENT_CHAIN'
END
WHERE w.`grade_resolve_mode` IS NULL
   OR w.`grade_resolve_mode` = ''
   OR w.`grade_resolve_mode` = 'CURRENT_CHAIN';
