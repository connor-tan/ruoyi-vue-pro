SET NAMES utf8mb4;

INSERT INTO `edu_year_catalog`
(`year_start`, `year_end`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(2025, 2026, 'system', NOW(), 'system', NOW(), b'0'),
(2026, 2027, 'system', NOW(), 'system', NOW(), b'0')
ON DUPLICATE KEY UPDATE
`updater` = VALUES(`updater`),
`update_time` = VALUES(`update_time`),
`deleted` = VALUES(`deleted`);

SET @restore_year_catalog_2025 := (
  SELECT `id`
  FROM `edu_year_catalog`
  WHERE `year_start` = 2025
    AND `year_end` = 2026
    AND `deleted` = b'0'
  ORDER BY `id` DESC
  LIMIT 1
);
SET @restore_year_catalog_2026 := (
  SELECT `id`
  FROM `edu_year_catalog`
  WHERE `year_start` = 2026
    AND `year_end` = 2027
    AND `deleted` = b'0'
  ORDER BY `id` DESC
  LIMIT 1
);

-- Restore subscription built-in window templates.
INSERT INTO `sub_window_template`
(`id`, `code`, `name`, `target_period`, `grade_calc_rule`, `grade_resolve_mode`, `description`, `status`, `sort`, `built_in`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(1, 'NEW_YEAR_PRE_SALE', '新学年预售', 'FULL_YEAR', 'PROMOTED_GRADE', 'TARGET_CLASS_FIRST', '适用于新学年开学前预售，按升学后年级匹配刊物。', 0, 10, b'1', NULL, 'system', NOW(), 'system', NOW(), b'0'),
(2, 'BACK_TO_SCHOOL_RESTOCK', '开学补订', 'FULL_YEAR', 'CURRENT_GRADE', 'CURRENT_CHAIN', '适用于开学后的补订窗口，按当前学籍年级匹配刊物。', 0, 20, b'1', NULL, 'system', NOW(), 'system', NOW(), b'0'),
(3, 'SECOND_TERM_SUBSCRIPTION', '下学期订刊', 'SECOND_TERM', 'CURRENT_GRADE', 'CURRENT_CHAIN', '适用于下学期订刊窗口，按当前学籍年级匹配刊物。', 0, 30, b'1', NULL, 'system', NOW(), 'system', NOW(), b'0')
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`target_period` = VALUES(`target_period`),
`grade_calc_rule` = VALUES(`grade_calc_rule`),
`grade_resolve_mode` = VALUES(`grade_resolve_mode`),
`description` = VALUES(`description`),
`status` = VALUES(`status`),
`sort` = VALUES(`sort`),
`built_in` = VALUES(`built_in`),
`remark` = VALUES(`remark`),
`updater` = VALUES(`updater`),
`update_time` = VALUES(`update_time`),
`deleted` = VALUES(`deleted`);

-- Restore target school years for the existing schools.
INSERT INTO `edu_school_year`
(`school_id`, `year_catalog_id`, `year_start`, `year_end`, `start_date`, `end_date`, `creator`, `updater`, `deleted`)
SELECT `id`, @restore_year_catalog_2025, 2025, 2026, '2025-09-01', '2026-06-30', 'system', 'system', b'0'
FROM `edu_school`
WHERE `deleted` = b'0'
  AND NOT EXISTS (
    SELECT 1
    FROM `edu_school_year`
    WHERE `edu_school_year`.`school_id` = `edu_school`.`id`
      AND `edu_school_year`.`year_start` = 2025
      AND `edu_school_year`.`deleted` = b'0'
  );

INSERT INTO `edu_school_year`
(`school_id`, `year_catalog_id`, `year_start`, `year_end`, `start_date`, `end_date`, `creator`, `updater`, `deleted`)
SELECT `id`, @restore_year_catalog_2026, 2026, 2027, '2026-09-01', '2027-06-30', 'system', 'system', b'0'
FROM `edu_school`
WHERE `deleted` = b'0'
  AND NOT EXISTS (
    SELECT 1
    FROM `edu_school_year`
    WHERE `edu_school_year`.`school_id` = `edu_school`.`id`
      AND `edu_school_year`.`year_start` = 2026
      AND `edu_school_year`.`deleted` = b'0'
  );
