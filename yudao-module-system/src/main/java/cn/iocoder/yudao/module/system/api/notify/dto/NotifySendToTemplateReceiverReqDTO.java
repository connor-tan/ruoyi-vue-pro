package cn.iocoder.yudao.module.system.api.notify.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;

/**
 * 站内信发送给模板默认接收人
 */
@Data
public class NotifySendToTemplateReceiverReqDTO {

    /**
     * 站内信模板编号
     */
    @NotEmpty(message = "站内信模板编号不能为空")
    private String templateCode;

    /**
     * 站内信模板参数
     */
    private Map<String, Object> templateParams;

}
