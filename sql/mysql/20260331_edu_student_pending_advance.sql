-- 学生末级结转为待升学
-- 1. 扩充 edu_student_flow.change_type，支持 PENDING_ADVANCE

ALTER TABLE `edu_student_flow`
  MODIFY COLUMN `change_type` enum('ENROLL','PROMOTE','TRANSFER','REPEAT','GRADUATE','PENDING_ADVANCE')
  DEFAULT NULL COMMENT '变更类型';
