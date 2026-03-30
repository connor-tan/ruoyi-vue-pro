package cn.iocoder.yudao.module.edu.dal.dataobject.student;

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

/**
 * 学生全局升班任务 DO
 */
@TableName("edu_student_promotion_task")
@KeySequence("edu_student_promotion_task_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentPromotionTaskDO extends BaseDO {

    @TableId
    private Long id;

    private Integer fromYearStart;

    private Integer toYearStart;

    private String scopeType;

    private String scopeSnapshot;

    private Boolean autoCreateClass;

    private Boolean graduateTerminalStudent;

    private Integer totalSchoolCount;

    private Integer successSchoolCount;

    private Integer skippedSchoolCount;

    private Integer failedSchoolCount;

    private Integer totalCount;

    private Integer promotedCount;

    private Integer repeatCount;

    private Integer graduatedCount;

    private Integer skippedCount;

    private Integer status;

    private String remark;

}
