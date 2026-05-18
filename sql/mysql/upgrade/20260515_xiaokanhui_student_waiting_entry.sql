-- APP 家长绑定孩子：待入学状态、学生状态字典、待入学自动转在读任务

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE edu_student
    MODIFY COLUMN status tinyint DEFAULT 1 COMMENT '1:在读；2:毕业；3:休学；4:待升学；5:待入学';

SET @student_status_dict_type := 'edu_student_status';

UPDATE system_dict_type
SET name = '学生状态',
    status = 0,
    remark = 'EDU 学生状态',
    updater = '1',
    update_time = NOW(),
    deleted = b'0',
    deleted_time = NULL
WHERE type = @student_status_dict_type;

INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time)
SELECT '学生状态', @student_status_dict_type, 0, 'EDU 学生状态', '1', NOW(), '1', NOW(), b'0', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM system_dict_type WHERE type = @student_status_dict_type
);

UPDATE system_dict_data
SET sort = 1, label = '在读', status = 0, color_type = 'success', css_class = '', remark = '', updater = '1', update_time = NOW(), deleted = b'0'
WHERE dict_type = @student_status_dict_type AND value = '1';
INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 1, '在读', '1', @student_status_dict_type, 0, 'success', '', '', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = @student_status_dict_type AND value = '1');

UPDATE system_dict_data
SET sort = 2, label = '毕业', status = 0, color_type = 'info', css_class = '', remark = '', updater = '1', update_time = NOW(), deleted = b'0'
WHERE dict_type = @student_status_dict_type AND value = '2';
INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 2, '毕业', '2', @student_status_dict_type, 0, 'info', '', '', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = @student_status_dict_type AND value = '2');

UPDATE system_dict_data
SET sort = 3, label = '休学', status = 0, color_type = 'warning', css_class = '', remark = '', updater = '1', update_time = NOW(), deleted = b'0'
WHERE dict_type = @student_status_dict_type AND value = '3';
INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 3, '休学', '3', @student_status_dict_type, 0, 'warning', '', '', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = @student_status_dict_type AND value = '3');

UPDATE system_dict_data
SET sort = 4, label = '待升学', status = 0, color_type = 'primary', css_class = '', remark = '', updater = '1', update_time = NOW(), deleted = b'0'
WHERE dict_type = @student_status_dict_type AND value = '4';
INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 4, '待升学', '4', @student_status_dict_type, 0, 'primary', '', '', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = @student_status_dict_type AND value = '4');

UPDATE system_dict_data
SET sort = 5, label = '待入学', status = 0, color_type = 'primary', css_class = '', remark = '', updater = '1', update_time = NOW(), deleted = b'0'
WHERE dict_type = @student_status_dict_type AND value = '5';
INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 5, '待入学', '5', @student_status_dict_type, 0, 'primary', '', '', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = @student_status_dict_type AND value = '5');

SET @waiting_entry_job_handler := 'eduStudentWaitingEntryActivateJob';

UPDATE infra_job
SET name = '待入学学生自动转在读 Job',
    status = 1,
    handler_param = '',
    cron_expression = '0 10 0 * * ?',
    retry_count = 0,
    retry_interval = 0,
    monitor_timeout = 0,
    updater = '1',
    update_time = NOW(),
    deleted = b'0'
WHERE handler_name = @waiting_entry_job_handler
  AND deleted = b'0';

INSERT INTO infra_job (name, status, handler_name, handler_param, cron_expression, retry_count, retry_interval,
                       monitor_timeout, creator, create_time, updater, update_time, deleted)
SELECT '待入学学生自动转在读 Job', 1, @waiting_entry_job_handler, '', '0 10 0 * * ?', 0, 0, 0,
       '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM infra_job WHERE handler_name = @waiting_entry_job_handler AND deleted = b'0'
);
