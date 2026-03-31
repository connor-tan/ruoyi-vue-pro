-- edu 学生相关查询优化索引
-- 1. 学生主表分页 / 学校统计
-- 2. 学生班级当前记录查询
-- 3. 学生流转历史查询
-- 4. 学校班级按学校 + 学年查询

ALTER TABLE `edu_student`
    ADD INDEX `idx_edu_student_school_status_entry_id` (`current_school_id`, `status`, `entry_year`, `id`),
    ADD INDEX `idx_edu_student_parent_id` (`belong_to`, `id`),
    ADD INDEX `idx_edu_student_school_student_code` (`current_school_id`, `student_code`);

ALTER TABLE `edu_student_class`
    ADD INDEX `idx_edu_student_class_student_current` (`student_id`, `end_date`, `start_date`),
    ADD INDEX `idx_edu_student_class_class_current` (`class_id`, `end_date`, `start_date`, `student_id`);

ALTER TABLE `edu_student_flow`
    ADD INDEX `idx_edu_student_flow_batch_effective_id` (`batch_id`, `effective_date`, `id`);

ALTER TABLE `edu_school_class`
    ADD INDEX `idx_edu_school_class_school_year_entry_class` (`school_id`, `school_year_id`, `entry_year`, `class_no`);
