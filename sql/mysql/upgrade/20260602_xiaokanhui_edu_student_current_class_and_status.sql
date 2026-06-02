-- 校刊汇 EDU：学生当前班级快照、状态字典和核心索引
-- 口径：
-- 1. edu_student.current_class_id 是冗余快照，由学生绑定、升班、回滚等写流程维护。
-- 2. 当前班级解析按业务日期命中学生班级区间，不以 end_date IS NULL 作为唯一口径。
-- 3. 现存 bit(1) 业务布尔字段已由后端 Boolean 明确映射，本次不迁移。

SET NAMES utf8mb4;

SET @sql := (
    SELECT IF(COUNT(*) = 0,
              'ALTER TABLE edu_student ADD COLUMN current_class_id bigint DEFAULT NULL COMMENT ''当前班级快照，由学生班级流转流程维护'' AFTER current_school_id',
              'SELECT ''edu_student.current_class_id exists''')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'edu_student'
      AND COLUMN_NAME = 'current_class_id'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE edu_student s
LEFT JOIN (
    SELECT current_sc.student_id, current_sc.class_id
    FROM (
        SELECT sc.student_id,
               sc.class_id,
               COUNT(*) OVER (PARTITION BY sc.student_id) AS current_count
        FROM edu_student_class sc
        JOIN edu_school_class c ON c.id = sc.class_id AND c.deleted = b'0'
        WHERE sc.deleted = b'0'
          AND sc.start_date <= CURDATE()
          AND (sc.end_date IS NULL OR sc.end_date >= CURDATE())
    ) current_sc
    WHERE current_sc.current_count = 1
) resolved ON resolved.student_id = s.id
LEFT JOIN edu_school_class c ON c.id = resolved.class_id AND c.deleted = b'0'
SET s.current_class_id = resolved.class_id,
    s.current_school_id = COALESCE(c.school_id, s.current_school_id),
    s.updater = 'system',
    s.update_time = NOW()
WHERE s.deleted = b'0'
  AND (
      NOT (s.current_class_id <=> resolved.class_id)
      OR (c.school_id IS NOT NULL AND NOT (s.current_school_id <=> c.school_id))
  );

ALTER TABLE edu_student
    MODIFY COLUMN status tinyint DEFAULT 1 COMMENT '学生状态：1在读；2毕业；3休学；4待升学；5待入学';

ALTER TABLE edu_student_flow
    MODIFY COLUMN status tinyint NOT NULL DEFAULT 1 COMMENT '流转状态：1有效；2已回滚',
    MODIFY COLUMN target_class_created bit(1) NOT NULL DEFAULT b'0' COMMENT '目标班级是否由本次任务自动创建，后端 Boolean 映射';

ALTER TABLE edu_student_promotion_task
    MODIFY COLUMN status tinyint NOT NULL DEFAULT 0 COMMENT '任务状态：0执行中；1成功；2部分成功；3失败；4已回滚',
    MODIFY COLUMN auto_create_class bit(1) NOT NULL DEFAULT b'0' COMMENT '是否自动创建目标班级，后端 Boolean 映射',
    MODIFY COLUMN graduate_terminal_student bit(1) NOT NULL DEFAULT b'1' COMMENT '末级学生是否自动待升学，后端 Boolean 映射';

ALTER TABLE edu_student_promotion_batch
    MODIFY COLUMN status tinyint NOT NULL DEFAULT 0 COMMENT '批次状态：1成功；2跳过；3失败；4已回滚',
    MODIFY COLUMN auto_create_class bit(1) NOT NULL DEFAULT b'0' COMMENT '是否自动创建目标班级，后端 Boolean 映射',
    MODIFY COLUMN graduate_terminal_student bit(1) NOT NULL DEFAULT b'1' COMMENT '末级学生是否自动待升学，后端 Boolean 映射';

ALTER TABLE edu_school_class
    MODIFY COLUMN school_grade_id bigint NOT NULL COMMENT '学校年级ID',
    MODIFY COLUMN school_year_id bigint NOT NULL COMMENT '学校学年ID';

SET @sql := (
    SELECT IF(COUNT(*) = 0,
              'ALTER TABLE edu_student ADD INDEX idx_edu_student_school_class_status_id (current_school_id, current_class_id, status, id)',
              'SELECT ''idx_edu_student_school_class_status_id exists''')
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'edu_student'
      AND INDEX_NAME = 'idx_edu_student_school_class_status_id'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(COUNT(*) = 0,
              'ALTER TABLE edu_school_class ADD INDEX idx_edu_school_class_school_year_grade_no (school_id, school_year_id, school_grade_id, class_no, id)',
              'SELECT ''idx_edu_school_class_school_year_grade_no exists''')
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'edu_school_class'
      AND INDEX_NAME = 'idx_edu_school_class_school_year_grade_no'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(COUNT(*) = 0,
              'ALTER TABLE edu_student_class ADD INDEX idx_edu_student_class_student_class_date (student_id, class_id, start_date, end_date)',
              'SELECT ''idx_edu_student_class_student_class_date exists''')
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'edu_student_class'
      AND INDEX_NAME = 'idx_edu_student_class_student_class_date'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(COUNT(*) = 0,
              'ALTER TABLE edu_student_promotion_batch ADD INDEX idx_edu_student_promotion_batch_school_year_status (school_id, from_school_year_id, to_school_year_id, status, id)',
              'SELECT ''idx_edu_student_promotion_batch_school_year_status exists''')
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'edu_student_promotion_batch'
      AND INDEX_NAME = 'idx_edu_student_promotion_batch_school_year_status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @dict_type := CONVERT('edu_student_flow_status' USING utf8mb4) COLLATE utf8mb4_unicode_ci;
UPDATE system_dict_type
SET name = '学生流转状态', status = 0, remark = 'EDU 学生流转状态', updater = '1', update_time = NOW(), deleted = b'0'
WHERE type = @dict_type;
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted)
SELECT '学生流转状态', @dict_type, 0, 'EDU 学生流转状态', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_type WHERE type = @dict_type);
UPDATE system_dict_data SET sort = 1, label = '有效', status = 0, color_type = 'success', updater = '1', update_time = NOW(), deleted = b'0'
WHERE dict_type = @dict_type AND value = '1';
INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 1, '有效', '1', @dict_type, 0, 'success', '', '', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = @dict_type AND value = '1');
UPDATE system_dict_data SET sort = 2, label = '已回滚', status = 0, color_type = 'info', updater = '1', update_time = NOW(), deleted = b'0'
WHERE dict_type = @dict_type AND value = '2';
INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 2, '已回滚', '2', @dict_type, 0, 'info', '', '', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = @dict_type AND value = '2');

SET @dict_type := CONVERT('edu_student_promotion_task_status' USING utf8mb4) COLLATE utf8mb4_unicode_ci;
UPDATE system_dict_type
SET name = '学生升班任务状态', status = 0, remark = 'EDU 学生全局升班任务状态', updater = '1', update_time = NOW(), deleted = b'0'
WHERE type = @dict_type;
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted)
SELECT '学生升班任务状态', @dict_type, 0, 'EDU 学生全局升班任务状态', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_type WHERE type = @dict_type);
UPDATE system_dict_data SET sort = 0, label = '执行中', status = 0, color_type = 'primary', updater = '1', update_time = NOW(), deleted = b'0'
WHERE dict_type = @dict_type AND value = '0';
INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 0, '执行中', '0', @dict_type, 0, 'primary', '', '', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = @dict_type AND value = '0');
UPDATE system_dict_data SET sort = 1, label = '成功', status = 0, color_type = 'success', updater = '1', update_time = NOW(), deleted = b'0'
WHERE dict_type = @dict_type AND value = '1';
INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 1, '成功', '1', @dict_type, 0, 'success', '', '', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = @dict_type AND value = '1');
UPDATE system_dict_data SET sort = 2, label = '部分成功', status = 0, color_type = 'warning', updater = '1', update_time = NOW(), deleted = b'0'
WHERE dict_type = @dict_type AND value = '2';
INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 2, '部分成功', '2', @dict_type, 0, 'warning', '', '', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = @dict_type AND value = '2');
UPDATE system_dict_data SET sort = 3, label = '失败', status = 0, color_type = 'danger', updater = '1', update_time = NOW(), deleted = b'0'
WHERE dict_type = @dict_type AND value = '3';
INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 3, '失败', '3', @dict_type, 0, 'danger', '', '', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = @dict_type AND value = '3');
UPDATE system_dict_data SET sort = 4, label = '已回滚', status = 0, color_type = 'info', updater = '1', update_time = NOW(), deleted = b'0'
WHERE dict_type = @dict_type AND value = '4';
INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 4, '已回滚', '4', @dict_type, 0, 'info', '', '', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = @dict_type AND value = '4');

SET @dict_type := CONVERT('edu_student_promotion_batch_status' USING utf8mb4) COLLATE utf8mb4_unicode_ci;
UPDATE system_dict_type
SET name = '学生升班批次状态', status = 0, remark = 'EDU 学生升班批次状态', updater = '1', update_time = NOW(), deleted = b'0'
WHERE type = @dict_type;
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted)
SELECT '学生升班批次状态', @dict_type, 0, 'EDU 学生升班批次状态', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_type WHERE type = @dict_type);
UPDATE system_dict_data SET sort = 1, label = '成功', status = 0, color_type = 'success', updater = '1', update_time = NOW(), deleted = b'0'
WHERE dict_type = @dict_type AND value = '1';
INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 1, '成功', '1', @dict_type, 0, 'success', '', '', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = @dict_type AND value = '1');
UPDATE system_dict_data SET sort = 2, label = '跳过', status = 0, color_type = 'info', updater = '1', update_time = NOW(), deleted = b'0'
WHERE dict_type = @dict_type AND value = '2';
INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 2, '跳过', '2', @dict_type, 0, 'info', '', '', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = @dict_type AND value = '2');
UPDATE system_dict_data SET sort = 3, label = '失败', status = 0, color_type = 'danger', updater = '1', update_time = NOW(), deleted = b'0'
WHERE dict_type = @dict_type AND value = '3';
INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 3, '失败', '3', @dict_type, 0, 'danger', '', '', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = @dict_type AND value = '3');
UPDATE system_dict_data SET sort = 4, label = '已回滚', status = 0, color_type = 'info', updater = '1', update_time = NOW(), deleted = b'0'
WHERE dict_type = @dict_type AND value = '4';
INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 4, '已回滚', '4', @dict_type, 0, 'info', '', '', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = @dict_type AND value = '4');

SET @dict_type := CONVERT('edu_global_promotion_school_status' USING utf8mb4) COLLATE utf8mb4_unicode_ci;
UPDATE system_dict_type
SET name = '全局升班单校状态', status = 0, remark = 'EDU 全局升班单校预览/执行状态', updater = '1', update_time = NOW(), deleted = b'0'
WHERE type = @dict_type;
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted)
SELECT '全局升班单校状态', @dict_type, 0, 'EDU 全局升班单校预览/执行状态', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_type WHERE type = @dict_type);
UPDATE system_dict_data SET sort = 1, label = '待执行', status = 0, color_type = 'primary', updater = '1', update_time = NOW(), deleted = b'0'
WHERE dict_type = @dict_type AND value = 'READY';
INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 1, '待执行', 'READY', @dict_type, 0, 'primary', '', '', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = @dict_type AND value = 'READY');
UPDATE system_dict_data SET sort = 2, label = '跳过', status = 0, color_type = 'info', updater = '1', update_time = NOW(), deleted = b'0'
WHERE dict_type = @dict_type AND value = 'SKIP';
INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 2, '跳过', 'SKIP', @dict_type, 0, 'info', '', '', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = @dict_type AND value = 'SKIP');
UPDATE system_dict_data SET sort = 3, label = '成功', status = 0, color_type = 'success', updater = '1', update_time = NOW(), deleted = b'0'
WHERE dict_type = @dict_type AND value = 'SUCCESS';
INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 3, '成功', 'SUCCESS', @dict_type, 0, 'success', '', '', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = @dict_type AND value = 'SUCCESS');
UPDATE system_dict_data SET sort = 4, label = '失败', status = 0, color_type = 'danger', updater = '1', update_time = NOW(), deleted = b'0'
WHERE dict_type = @dict_type AND value = 'FAILED';
INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
SELECT 4, '失败', 'FAILED', @dict_type, 0, 'danger', '', '', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = @dict_type AND value = 'FAILED');
