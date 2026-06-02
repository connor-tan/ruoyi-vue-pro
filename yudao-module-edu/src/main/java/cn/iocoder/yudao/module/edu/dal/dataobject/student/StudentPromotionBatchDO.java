package cn.iocoder.yudao.module.edu.dal.dataobject.student;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.edu.enums.DictTypeConstants;
import cn.iocoder.yudao.module.edu.enums.StudentPromotionBatchStatusEnum;
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

    /**
     * MyBatis 明确映射为 Boolean，数据库现存 bit(1) 暂不迁移。
     */
    private Boolean autoCreateClass;

    /**
     * MyBatis 明确映射为 Boolean，数据库现存 bit(1) 暂不迁移。
     */
    private Boolean graduateTerminalStudent;

    private Integer totalCount;

    private Integer promotedCount;

    private Integer repeatCount;

    private Integer graduatedCount;

    private Integer skippedCount;

    /**
     * 枚举 {@link StudentPromotionBatchStatusEnum}
     * 字典 {@link DictTypeConstants#EDU_STUDENT_PROMOTION_BATCH_STATUS}
     */
    private Integer status;

    private String reason;

    private String remark;

}
