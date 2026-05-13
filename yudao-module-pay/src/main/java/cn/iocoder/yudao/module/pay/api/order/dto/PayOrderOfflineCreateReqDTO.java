package cn.iocoder.yudao.module.pay.api.order.dto;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

@Data
public class PayOrderOfflineCreateReqDTO implements Serializable {

    @NotNull(message = "应用标识不能为空")
    private String appKey;

    @NotEmpty(message = "用户 IP 不能为空")
    private String userIp;

    private Long userId;

    @InEnum(UserTypeEnum.class)
    private Integer userType;

    @NotEmpty(message = "商户订单编号不能为空")
    private String merchantOrderId;

    @NotEmpty(message = "商品标题不能为空")
    @Length(max = PayOrderCreateReqDTO.SUBJECT_MAX_LENGTH, message = "商品标题不能超过 32")
    private String subject;

    @Length(max = 128, message = "商品描述信息长度不能超过128")
    private String body;

    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "支付金额必须大于零")
    private Integer price;

}
