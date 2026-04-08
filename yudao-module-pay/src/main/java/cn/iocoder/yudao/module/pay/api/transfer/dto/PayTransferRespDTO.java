package cn.iocoder.yudao.module.pay.api.transfer.dto;

import cn.iocoder.yudao.module.pay.enums.PayChannelEnum;
import cn.iocoder.yudao.module.pay.enums.transfer.PayTransferStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class PayTransferRespDTO {

    /**
     * 编号
     */
    private Long id;

    /**
     * 转账单号
     */
    private String no;

    /**
     * 转账渠道编码
     *
     * 枚举 {@link PayChannelEnum}
     */
    private String channelCode;

    // ========== 商户相关字段 ==========

    /**
     * 商户转账单编号
     */
    private String merchantTransferId;

    // ========== 转账相关字段 ==========

    /**
     * 转账金额，单位：分
     */
    private Integer price;

    /**
     * 转账状态
     *
     * 枚举 {@link PayTransferStatusEnum}
     */
    private Integer status;

    /**
     * 订单转账成功时间
     */
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime successTime;

    // ========== 其它字段 ==========

    /**
     * 调用渠道的错误码
     */
    private String channelErrorCode;
    /**
     * 调用渠道的错误提示
     */
    private String channelErrorMsg;

    /**
     * 渠道 package 信息
     *
     * 特殊：目前只有微信转账有这个东西！！！
     * @see <a href="https://pay.weixin.qq.com/doc/v3/merchant/4012716430">JSAPI 调起用户确认收款</a>
     */
    private String channelPackageInfo;
    /**
     * 渠道商户号
     *
     * 特殊：目前只有微信转账有这个东西！！！
     * @see <a href="https://pay.weixin.qq.com/doc/v3/merchant/4012716430">JSAPI 调起用户确认收款</a>
     */
    private String channelMchId;

}
