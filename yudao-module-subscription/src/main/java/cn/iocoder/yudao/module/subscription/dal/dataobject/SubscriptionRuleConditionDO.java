package cn.iocoder.yudao.module.subscription.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("subscription_rule_condition")
@KeySequence("subscription_rule_condition_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRuleConditionDO extends BaseDO {

    @TableId
    private Long id;

    private Long ruleId;

    private String factor;

    private String operator;

    private String value;

    private String valueName;

}
