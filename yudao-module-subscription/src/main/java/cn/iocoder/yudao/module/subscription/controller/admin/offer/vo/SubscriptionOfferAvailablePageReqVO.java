package cn.iocoder.yudao.module.subscription.controller.admin.offer.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SubscriptionOfferAvailablePageReqVO extends PageParam {

    @NotNull(message = "窗口编号不能为空")
    private Long windowId;

    private String productName;

    private List<Long> categoryIds;

    private Long publisherId;

    private Long publicationTypeId;

    private String issueCycle;

    private Long gradeCatalogId;

    private String candidateStatus;

}
