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
 * 学生一键升班批次 DO
 */
@TableName("edu_student_promotion_batch")
@KeySequence("edu_student_promotion_batch_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentPromotionBatchDO extends BaseDO {

    @TableId
    private Long id;

    private Long taskId;

    private Long schoolId;

    private Long fromSchoolYearId;

    private Long toSchoolYearId;

    private Boolean autoCreateClass;

    private Boolean graduateTerminalStudent;

    private Integer totalCount;

    private Integer promotedCount;

    private Integer repeatCount;

    private Integer graduatedCount;

    private Integer skippedCount;

    private Integer status;

    private String reason;

    private String remark;

}
