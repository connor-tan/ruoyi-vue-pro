package cn.iocoder.yudao.module.subscription.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDate;

@TableName("subscription_offer_sku_issue")
@KeySequence("subscription_offer_sku_issue_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionOfferSkuIssueDO extends BaseDO {

    @TableId
    private Long id;

    private Long offerId;

    private Long offerSkuId;

    private Integer issueNo;

    private String issueName;

    private LocalDate plannedPublishDate;

    private LocalDate plannedDeliveryDate;

    private Integer sort;

    private Integer status;

    private String remark;

}
