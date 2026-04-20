-- Edu 学年班级自动生成任务
-- 1. 保证学校学年按 school_id + year_start 幂等
-- 2. 预置 Infra 定时任务配置，默认每年 7 月 1 日 02:00 执行

ALTER TABLE `edu_school_year`
    ADD UNIQUE KEY `uk_edu_school_year_school_start` (`school_id`, `year_start`);

INSERT INTO `infra_job` (
    `name`,
    `status`,
    `handler_name`,
    `handler_param`,
    `cron_expression`,
    `retry_count`,
    `retry_interval`,
    `monitor_timeout`,
    `creator`,
    `updater`,
    `deleted`
)
SELECT
    'Edu 学年班级自动生成 Job',
    1,
    'eduSchoolYearClassGenerateJob',
    '',
    '0 0 2 1 7 ?',
    1,
    60,
    0,
    'system',
    'system',
    b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM `infra_job`
    WHERE `handler_name` = 'eduSchoolYearClassGenerateJob'
      AND `deleted` = b'0'
);
