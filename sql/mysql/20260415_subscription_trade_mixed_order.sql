SET NAMES utf8mb4;

ALTER TABLE `trade_cart`
  ADD COLUMN `subscription_student_id` bigint DEFAULT NULL COMMENT '订刊学生编号' AFTER `selected`,
  ADD COLUMN `subscription_window_sku_id` bigint DEFAULT NULL COMMENT '订刊窗口 SKU 编号' AFTER `subscription_student_id`;

ALTER TABLE `trade_cart`
  ADD KEY `idx_trade_cart_subscription` (`user_id`, `subscription_student_id`, `subscription_window_sku_id`);

ALTER TABLE `trade_order_item`
  ADD COLUMN `subscription_student_id` bigint DEFAULT NULL COMMENT '订刊学生编号' AFTER `after_sale_status`,
  ADD COLUMN `subscription_school_id` bigint DEFAULT NULL COMMENT '订刊学校编号' AFTER `subscription_student_id`,
  ADD COLUMN `subscription_grade_catalog_id` bigint DEFAULT NULL COMMENT '订刊解析年级编号' AFTER `subscription_school_id`,
  ADD COLUMN `subscription_window_id` bigint DEFAULT NULL COMMENT '订刊窗口编号' AFTER `subscription_grade_catalog_id`,
  ADD COLUMN `subscription_window_name_snapshot` varchar(128) DEFAULT NULL COMMENT '订刊窗口名称快照' AFTER `subscription_window_id`,
  ADD COLUMN `subscription_target_year_start` int DEFAULT NULL COMMENT '订刊目标学年开始年份' AFTER `subscription_window_name_snapshot`,
  ADD COLUMN `subscription_target_year_end` int DEFAULT NULL COMMENT '订刊目标学年结束年份' AFTER `subscription_target_year_start`,
  ADD COLUMN `subscription_target_period` varchar(32) DEFAULT NULL COMMENT '订刊目标周期' AFTER `subscription_target_year_end`,
  ADD COLUMN `subscription_window_spu_id` bigint DEFAULT NULL COMMENT '订刊窗口刊物编号' AFTER `subscription_target_period`,
  ADD COLUMN `subscription_window_sku_id` bigint DEFAULT NULL COMMENT '订刊窗口 SKU 编号' AFTER `subscription_window_spu_id`,
  ADD COLUMN `subscription_visibility_reason` varchar(64) DEFAULT NULL COMMENT '订刊可见原因' AFTER `subscription_window_sku_id`,
  ADD COLUMN `subscription_matched_rule_id` bigint DEFAULT NULL COMMENT '订刊命中特殊规则编号' AFTER `subscription_visibility_reason`,
  ADD COLUMN `subscription_grade_applicability_override` tinyint(1) DEFAULT NULL COMMENT '是否突破刊物商品适用年级' AFTER `subscription_matched_rule_id`;

ALTER TABLE `trade_order_item`
  ADD KEY `idx_trade_order_item_subscription_limit` (`subscription_student_id`, `subscription_window_sku_id`);
