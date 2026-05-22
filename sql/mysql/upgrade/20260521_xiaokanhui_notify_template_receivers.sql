-- 校刊汇：站内信模板默认接收人与交易通知模板

SET @schema_name := DATABASE();

SET @column_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'system_notify_template'
      AND COLUMN_NAME = 'receiver_user_ids'
);

SET @sql := IF(@column_exists = 0,
    'ALTER TABLE system_notify_template ADD COLUMN receiver_user_ids varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT ''[]'' COMMENT ''默认接收人用户编号数组'' AFTER params',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE system_notify_template
SET name = '用户提交订单通知',
    nickname = '校刊汇订单中心',
    content = '用户 {userId} 提交订单 {orderNo}，应付金额 {payPrice} 元，请及时关注。',
    type = 2,
    params = '["userId","orderNo","payPrice"]',
    status = 0,
    remark = 'app 用户提交订单成功后发送给模板默认接收人',
    updater = 'system',
    update_time = NOW(),
    receiver_user_ids = IFNULL(receiver_user_ids, '[]')
WHERE code = 'trade_order_created_admin';

INSERT INTO system_notify_template (
    name, code, nickname, content, type, params, receiver_user_ids, status, remark,
    creator, create_time, updater, update_time, deleted
)
SELECT '用户提交订单通知',
       'trade_order_created_admin',
       '校刊汇订单中心',
       '用户 {userId} 提交订单 {orderNo}，应付金额 {payPrice} 元，请及时关注。',
       2,
       '["userId","orderNo","payPrice"]',
       '[]',
       0,
       'app 用户提交订单成功后发送给模板默认接收人',
       'system',
       NOW(),
       'system',
       NOW(),
       b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM system_notify_template WHERE code = 'trade_order_created_admin'
);

UPDATE system_notify_template
SET name = '用户退款成功通知',
    nickname = '校刊汇订单中心',
    content = '用户 {userId} 的售后单 {afterSaleNo} 已退款成功，订单 {orderNo}，退款金额 {refundPrice} 元，请及时关注。',
    type = 2,
    params = '["userId","afterSaleNo","orderNo","refundPrice"]',
    status = 0,
    remark = '售后退款成功后发送给模板默认接收人',
    updater = 'system',
    update_time = NOW(),
    receiver_user_ids = IFNULL(receiver_user_ids, '[]')
WHERE code = 'trade_after_sale_refunded_admin';

INSERT INTO system_notify_template (
    name, code, nickname, content, type, params, receiver_user_ids, status, remark,
    creator, create_time, updater, update_time, deleted
)
SELECT '用户退款成功通知',
       'trade_after_sale_refunded_admin',
       '校刊汇订单中心',
       '用户 {userId} 的售后单 {afterSaleNo} 已退款成功，订单 {orderNo}，退款金额 {refundPrice} 元，请及时关注。',
       2,
       '["userId","afterSaleNo","orderNo","refundPrice"]',
       '[]',
       0,
       '售后退款成功后发送给模板默认接收人',
       'system',
       NOW(),
       'system',
       NOW(),
       b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM system_notify_template WHERE code = 'trade_after_sale_refunded_admin'
);
