package cn.iocoder.yudao.module.subscription.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("subscription_rule")
@KeySequence("subscription_rule_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRuleDO extends BaseDO {

    @TableId
    private Long id;

    private Long windowId;

    private Long offerId;

    private String name;

    private String effectType;

    private Boolean allowGradeOverride;

    private Integer status;

    private String remark;

}
