-- 学生状态定义统一为：
-- 1 = 在读
-- 2 = 毕业
-- 3 = 休学
-- 4 = 待升学

ALTER TABLE `edu_student`
    MODIFY COLUMN `status` tinyint DEFAULT '1' COMMENT '1:在读；2:毕业；3:休学；4:待升学';
