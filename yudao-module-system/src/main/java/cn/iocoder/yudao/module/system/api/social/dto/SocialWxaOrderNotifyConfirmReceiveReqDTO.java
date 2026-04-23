package cn.iocoder.yudao.module.system.api.social.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 小程序订单上传购物详情
 *
 * @see <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/shopping-order/normal-shopping-detail/uploadShoppingInfo.html">上传购物详情</a>
 * @author 芋道源码
 */
@Data
public class SocialWxaOrderNotifyConfirmReceiveReqDTO {

    /**
     * 原支付交易对应的微信订单号
     */
    @NotEmpty(message = "原支付交易对应的微信订单号不能为空")
    private String transactionId;

    /**
     * 快递签收时间
     */
    @NotNull(message = "快递签收时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime receivedTime;

}
