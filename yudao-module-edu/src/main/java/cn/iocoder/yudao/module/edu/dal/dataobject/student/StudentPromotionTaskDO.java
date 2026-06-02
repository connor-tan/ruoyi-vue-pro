package cn.iocoder.yudao.module.edu.dal.dataobject.student;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.edu.enums.DictTypeConstants;
import cn.iocoder.yudao.module.edu.enums.StudentPromotionTaskStatusEnum;
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

    /**
     * MyBatis 明确映射为 Boolean，数据库现存 bit(1) 暂不迁移。
     */
    private Boolean autoCreateClass;

    /**
     * MyBatis 明确映射为 Boolean，数据库现存 bit(1) 暂不迁移。
     */
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

    /**
     * 枚举 {@link StudentPromotionTaskStatusEnum}
     * 字典 {@link DictTypeConstants#EDU_STUDENT_PROMOTION_TASK_STATUS}
     */
    private Integer status;

    private String remark;

}
