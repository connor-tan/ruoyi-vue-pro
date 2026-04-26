-- 校刊汇刊物 SKU 册别 / 版本字典初始化
-- MySQL 8 幂等脚本：新增系统字典，并将既有中文自由文本映射为稳定字典值。

INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time)
SELECT '刊物册别', 'edu_publication_volume', 0, '校刊汇刊物 SKU 册别', 'admin', NOW(), 'admin', NOW(), b'0', NULL
WHERE NOT EXISTS (
  SELECT 1 FROM system_dict_type WHERE type = 'edu_publication_volume' AND deleted = b'0'
);

UPDATE system_dict_type
SET name = '刊物册别',
    status = 0,
    remark = '校刊汇刊物 SKU 册别',
    updater = 'admin',
    update_time = NOW()
WHERE type = 'edu_publication_volume'
  AND deleted = b'0';

INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time)
SELECT '刊物版本', 'edu_publication_edition', 0, '校刊汇刊物 SKU 版本', 'admin', NOW(), 'admin', NOW(), b'0', NULL
WHERE NOT EXISTS (
  SELECT 1 FROM system_dict_type WHERE type = 'edu_publication_edition' AND deleted = b'0'
);

UPDATE system_dict_type
SET name = '刊物版本',
    status = 0,
    remark = '校刊汇刊物 SKU 版本',
    updater = 'admin',
    update_time = NOW()
WHERE type = 'edu_publication_edition'
  AND deleted = b'0';

INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 1, '全册', 'FULL', 'edu_publication_volume', 0, '', '', '刊物册别 - 全册', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM system_dict_data WHERE dict_type = 'edu_publication_volume' AND value = 'FULL' AND deleted = b'0'
);

INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 2, '上册', 'FIRST_VOLUME', 'edu_publication_volume', 0, '', '', '刊物册别 - 上册', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM system_dict_data WHERE dict_type = 'edu_publication_volume' AND value = 'FIRST_VOLUME' AND deleted = b'0'
);

INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 3, '下册', 'SECOND_VOLUME', 'edu_publication_volume', 0, '', '', '刊物册别 - 下册', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM system_dict_data WHERE dict_type = 'edu_publication_volume' AND value = 'SECOND_VOLUME' AND deleted = b'0'
);

UPDATE system_dict_data
SET status = 0,
    sort = CASE value
      WHEN 'FULL' THEN 1
      WHEN 'FIRST_VOLUME' THEN 2
      WHEN 'SECOND_VOLUME' THEN 3
      ELSE sort
    END,
    label = CASE value
      WHEN 'FULL' THEN '全册'
      WHEN 'FIRST_VOLUME' THEN '上册'
      WHEN 'SECOND_VOLUME' THEN '下册'
      ELSE label
    END,
    updater = 'admin',
    update_time = NOW()
WHERE dict_type = 'edu_publication_volume'
  AND value IN ('FULL', 'FIRST_VOLUME', 'SECOND_VOLUME')
  AND deleted = b'0';

INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 1, '通用版', 'GENERAL', 'edu_publication_edition', 0, '', '', '刊物版本 - 通用版', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM system_dict_data WHERE dict_type = 'edu_publication_edition' AND value = 'GENERAL' AND deleted = b'0'
);

INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 2, '人教版', 'RENJIAO', 'edu_publication_edition', 0, '', '', '刊物版本 - 人教版', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM system_dict_data WHERE dict_type = 'edu_publication_edition' AND value = 'RENJIAO' AND deleted = b'0'
);

INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 3, '部编版', 'BU_BIAN', 'edu_publication_edition', 0, '', '', '刊物版本 - 部编版', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM system_dict_data WHERE dict_type = 'edu_publication_edition' AND value = 'BU_BIAN' AND deleted = b'0'
);

INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 4, '苏教版', 'SUJIAO', 'edu_publication_edition', 0, '', '', '刊物版本 - 苏教版', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM system_dict_data WHERE dict_type = 'edu_publication_edition' AND value = 'SUJIAO' AND deleted = b'0'
);

INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 5, '北师大版', 'BEISHIDA', 'edu_publication_edition', 0, '', '', '刊物版本 - 北师大版', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM system_dict_data WHERE dict_type = 'edu_publication_edition' AND value = 'BEISHIDA' AND deleted = b'0'
);

INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 6, '外研版', 'WAIYAN', 'edu_publication_edition', 0, '', '', '刊物版本 - 外研版', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM system_dict_data WHERE dict_type = 'edu_publication_edition' AND value = 'WAIYAN' AND deleted = b'0'
);

UPDATE system_dict_data
SET status = 0,
    sort = CASE value
      WHEN 'GENERAL' THEN 1
      WHEN 'RENJIAO' THEN 2
      WHEN 'BU_BIAN' THEN 3
      WHEN 'SUJIAO' THEN 4
      WHEN 'BEISHIDA' THEN 5
      WHEN 'WAIYAN' THEN 6
      ELSE sort
    END,
    label = CASE value
      WHEN 'GENERAL' THEN '通用版'
      WHEN 'RENJIAO' THEN '人教版'
      WHEN 'BU_BIAN' THEN '部编版'
      WHEN 'SUJIAO' THEN '苏教版'
      WHEN 'BEISHIDA' THEN '北师大版'
      WHEN 'WAIYAN' THEN '外研版'
      ELSE label
    END,
    updater = 'admin',
    update_time = NOW()
WHERE dict_type = 'edu_publication_edition'
  AND value IN ('GENERAL', 'RENJIAO', 'BU_BIAN', 'SUJIAO', 'BEISHIDA', 'WAIYAN')
  AND deleted = b'0';

UPDATE product_publication_sku_ext
SET volume_label = CASE volume_label
  WHEN '全册' THEN 'FULL'
  WHEN '全年' THEN 'FULL'
  WHEN '全学年' THEN 'FULL'
  WHEN '上册' THEN 'FIRST_VOLUME'
  WHEN '下册' THEN 'SECOND_VOLUME'
  ELSE volume_label
END
WHERE volume_label IN ('全册', '全年', '全学年', '上册', '下册');

UPDATE product_publication_sku_ext
SET edition_label = CASE edition_label
  WHEN '通用版' THEN 'GENERAL'
  WHEN '人教版' THEN 'RENJIAO'
  WHEN '部编版' THEN 'BU_BIAN'
  WHEN '苏教版' THEN 'SUJIAO'
  WHEN '北师大版' THEN 'BEISHIDA'
  WHEN '外研版' THEN 'WAIYAN'
  ELSE edition_label
END
WHERE edition_label IN ('通用版', '人教版', '部编版', '苏教版', '北师大版', '外研版');

UPDATE subscription_rule_condition
SET value_name = CASE value
  WHEN '全册' THEN '全册'
  WHEN '全年' THEN '全册'
  WHEN '全学年' THEN '全册'
  WHEN '上册' THEN '上册'
  WHEN '下册' THEN '下册'
  ELSE value_name
END,
    value = CASE value
  WHEN '全册' THEN 'FULL'
  WHEN '全年' THEN 'FULL'
  WHEN '全学年' THEN 'FULL'
  WHEN '上册' THEN 'FIRST_VOLUME'
  WHEN '下册' THEN 'SECOND_VOLUME'
  ELSE value
END
WHERE factor = 'SKU_VOLUME_LABEL'
  AND value IN ('全册', '全年', '全学年', '上册', '下册');

UPDATE subscription_rule_condition
SET value_name = CASE value
  WHEN 'FULL' THEN '全册'
  WHEN 'FIRST_VOLUME' THEN '上册'
  WHEN 'SECOND_VOLUME' THEN '下册'
  ELSE value_name
END
WHERE factor = 'SKU_VOLUME_LABEL'
  AND value IN ('FULL', 'FIRST_VOLUME', 'SECOND_VOLUME')
  AND (value_name IS NULL OR value_name = '' OR value_name = value);

UPDATE subscription_rule_condition
SET value_name = CASE value
  WHEN '通用版' THEN '通用版'
  WHEN '人教版' THEN '人教版'
  WHEN '部编版' THEN '部编版'
  WHEN '苏教版' THEN '苏教版'
  WHEN '北师大版' THEN '北师大版'
  WHEN '外研版' THEN '外研版'
  ELSE value_name
END,
    value = CASE value
  WHEN '通用版' THEN 'GENERAL'
  WHEN '人教版' THEN 'RENJIAO'
  WHEN '部编版' THEN 'BU_BIAN'
  WHEN '苏教版' THEN 'SUJIAO'
  WHEN '北师大版' THEN 'BEISHIDA'
  WHEN '外研版' THEN 'WAIYAN'
  ELSE value
END
WHERE factor = 'SKU_EDITION_LABEL'
  AND value IN ('通用版', '人教版', '部编版', '苏教版', '北师大版', '外研版');

UPDATE subscription_rule_condition
SET value_name = CASE value
  WHEN 'GENERAL' THEN '通用版'
  WHEN 'RENJIAO' THEN '人教版'
  WHEN 'BU_BIAN' THEN '部编版'
  WHEN 'SUJIAO' THEN '苏教版'
  WHEN 'BEISHIDA' THEN '北师大版'
  WHEN 'WAIYAN' THEN '外研版'
  ELSE value_name
END
WHERE factor = 'SKU_EDITION_LABEL'
  AND value IN ('GENERAL', 'RENJIAO', 'BU_BIAN', 'SUJIAO', 'BEISHIDA', 'WAIYAN')
  AND (value_name IS NULL OR value_name = '' OR value_name = value);
