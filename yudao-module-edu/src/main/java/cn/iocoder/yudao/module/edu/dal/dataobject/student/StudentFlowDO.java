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

import java.time.LocalDate;

/**
 * 学生流转日志 DO
 */
@TableName("edu_student_flow")
@KeySequence("edu_student_flow_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentFlowDO extends BaseDO {

    @TableId
    private Long id;

    private Long studentId;

    private Long batchId;

    private Long fromClassId;

    private Long toClassId;

    private String changeType;

    private LocalDate effectiveDate;

    private Integer status;

    private Boolean targetClassCreated;

    private String remark;

}
