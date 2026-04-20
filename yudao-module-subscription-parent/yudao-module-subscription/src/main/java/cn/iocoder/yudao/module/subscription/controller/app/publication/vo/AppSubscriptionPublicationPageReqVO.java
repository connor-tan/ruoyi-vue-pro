package cn.iocoder.yudao.module.subscription.controller.app.publication.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppSubscriptionPublicationPageReqVO extends PageParam {

    @NotNull(message = "学生不能为空")
    private Long studentId;

    private Long categoryId;

    private String keyword;
}
