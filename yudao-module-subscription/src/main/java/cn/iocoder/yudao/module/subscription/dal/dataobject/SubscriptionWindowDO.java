package cn.iocoder.yudao.module.subscription.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

@TableName("subscription_window")
@KeySequence("subscription_window_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionWindowDO extends BaseDO {

    @TableId
    private Long id;

    private String name;

    private Long targetYearCatalogId;

    private String targetYearNameSnapshot;

    private Integer targetYearStart;

    private Integer targetYearEnd;

    private String targetPeriod;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String gradeCalcRule;

    private String gradeResolveMode;

    private Integer status;

    private String remark;

}
