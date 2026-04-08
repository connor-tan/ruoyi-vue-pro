SET NAMES utf8mb4;

CREATE TABLE `sub_window` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(128) NOT NULL COMMENT '窗口名称',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `target_school_year_id` bigint NOT NULL COMMENT '目标学年编号',
  `target_semester` tinyint NOT NULL COMMENT '目标学期',
  `grade_calc_rule` varchar(32) NOT NULL COMMENT '年级计算规则',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_sub_window_status_time` (`status`,`start_time`,`end_time`)
) COMMENT='订刊窗口';
