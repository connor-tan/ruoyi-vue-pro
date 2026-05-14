-- 校刊汇：补齐交易配送类型字典

INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark,
                              creator, create_time, updater, update_time, deleted)
SELECT 3, '学校配送', '3', 'trade_delivery_type', 0, '', '', '', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM system_dict_data
  WHERE dict_type = 'trade_delivery_type' AND value = '3' AND deleted = b'0'
);

UPDATE system_dict_data
SET label = '学校配送',
    updater = '1',
    update_time = NOW()
WHERE dict_type = 'trade_delivery_type'
  AND value = '3'
  AND deleted = b'0';

INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark,
                              creator, create_time, updater, update_time, deleted)
SELECT 4, '混合配送', '4', 'trade_delivery_type', 0, '', '', '', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM system_dict_data
  WHERE dict_type = 'trade_delivery_type' AND value = '4' AND deleted = b'0'
);
