package cn.iocoder.yudao.module.system.api.notify;

import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendToTemplateReceiverReqDTO;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 站内信发送 API 接口
 *
 * @author xrcoder
 */
public interface NotifyMessageSendApi {

    /**
     * 发送单条站内信给 Admin 用户
     *
     * @param reqDTO 发送请求
     * @return 发送消息 ID
     */
    Long sendSingleMessageToAdmin(@Valid NotifySendSingleToUserReqDTO reqDTO);

    /**
     * 发送单条站内信给 Member 用户
     *
     * @param reqDTO 发送请求
     * @return 发送消息 ID
     */
    Long sendSingleMessageToMember(@Valid NotifySendSingleToUserReqDTO reqDTO);

    /**
     * 发送站内信给模板配置的 Admin 默认接收人
     *
     * @param reqDTO 发送请求
     * @return 发送消息 ID 集合
     */
    List<Long> sendMessageToAdminTemplateReceivers(@Valid NotifySendToTemplateReceiverReqDTO reqDTO);

}
