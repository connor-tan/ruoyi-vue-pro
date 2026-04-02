package cn.iocoder.yudao.module.subscription.dal.dataobject.window;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@TableName("sub_window")
@KeySequence("sub_window_seq")
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

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long targetSchoolYearId;

    private Integer targetSemester;

    private String gradeCalcRule;

    private Integer status;

    private String remark;
}
