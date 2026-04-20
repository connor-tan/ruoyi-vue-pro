SET NAMES utf8mb4;

-- edu + subscription 测试数据清理脚本
-- 适用当前本地联调环境，目标是清空教育业务数据和订刊业务数据，仅保留：
--   1. edu_grade_catalog 标准年级目录
--   2. sub_window_template 中 built_in = 1 的内置规则模板
--   3. 表结构、菜单权限、系统配置、商品中心刊物主数据

START TRANSACTION;

-- 先清理订刊中心业务数据
DELETE FROM `sub_window_sku`;
DELETE FROM `sub_window_spu_rule`;
DELETE FROM `sub_window_spu_grade`;
DELETE FROM `sub_window_spu`;
DELETE FROM `sub_window`;

DELETE FROM `sub_window_template`
WHERE `built_in` = b'0';

-- 再清理 edu 业务数据，保留标准年级目录
DELETE FROM `edu_student_promotion_task`;
DELETE FROM `edu_student_promotion_batch`;
DELETE FROM `edu_student_flow`;
DELETE FROM `edu_student_class`;
DELETE FROM `edu_student`;
DELETE FROM `edu_school_class`;
DELETE FROM `edu_school_year`;
DELETE FROM `edu_school_grade`;
DELETE FROM `edu_school`;
DELETE FROM `edu_station`;

ALTER TABLE `sub_window_sku` AUTO_INCREMENT = 1;
ALTER TABLE `sub_window_spu_rule` AUTO_INCREMENT = 1;
ALTER TABLE `sub_window_spu_grade` AUTO_INCREMENT = 1;
ALTER TABLE `sub_window_spu` AUTO_INCREMENT = 1;
ALTER TABLE `sub_window` AUTO_INCREMENT = 1;

ALTER TABLE `edu_student_promotion_task` AUTO_INCREMENT = 1;
ALTER TABLE `edu_student_promotion_batch` AUTO_INCREMENT = 1;
ALTER TABLE `edu_student_flow` AUTO_INCREMENT = 1;
ALTER TABLE `edu_student_class` AUTO_INCREMENT = 1;
ALTER TABLE `edu_student` AUTO_INCREMENT = 1;
ALTER TABLE `edu_school_class` AUTO_INCREMENT = 1;
ALTER TABLE `edu_school_year` AUTO_INCREMENT = 1;
ALTER TABLE `edu_school_grade` AUTO_INCREMENT = 1;
ALTER TABLE `edu_school` AUTO_INCREMENT = 1;
ALTER TABLE `edu_station` AUTO_INCREMENT = 1;

SET @next_template_auto_increment = (
    SELECT GREATEST(IFNULL(MAX(`id`), 0) + 1, 1)
    FROM `sub_window_template`
);
SET @alter_template_auto_increment_sql = CONCAT(
    'ALTER TABLE `sub_window_template` AUTO_INCREMENT = ',
    @next_template_auto_increment
);
PREPARE stmt FROM @alter_template_auto_increment_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

COMMIT;
