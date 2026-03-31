ALTER TABLE `edu_grade_catalog`
    ADD COLUMN `alias_name` varchar(64) DEFAULT NULL COMMENT '年级别名' AFTER `grade_name`;

UPDATE `edu_grade_catalog`
SET `alias_name` = '七年级'
WHERE `stage` = 'middle' AND `grade_no` = 'M1';

UPDATE `edu_grade_catalog`
SET `alias_name` = '八年级'
WHERE `stage` = 'middle' AND `grade_no` = 'M2';

UPDATE `edu_grade_catalog`
SET `alias_name` = '九年级'
WHERE `stage` = 'middle' AND `grade_no` = 'M3';
