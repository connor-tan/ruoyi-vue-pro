package cn.iocoder.yudao.module.subscription.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("subscription_window_offer_sku")
@KeySequence("subscription_window_offer_sku_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionWindowOfferSkuDO extends BaseDO {

    @TableId
    private Long id;

    private Long offerId;

    private Long productSkuId;

    private Integer sort;

    private Integer status;

    private Integer maxQuantityPerStudent;

    private String remark;

}
