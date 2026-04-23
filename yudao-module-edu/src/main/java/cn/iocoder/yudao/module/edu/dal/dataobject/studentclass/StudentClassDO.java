package cn.iocoder.yudao.module.edu.dal.dataobject.studentclass;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
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
 * 学生班级区间记录 DO
 *
 * @author connor
 */
@TableName("edu_student_class")
@KeySequence("edu_student_class_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentClassDO extends BaseDO {

    /**
     * 学生班级记录编号
     */
    @TableId
    private Long id;
    /**
     * 学生编号
     */
    private Long studentId;
    /**
     * 班级编号
     */
    private Long classId;
    /**
     * 入班日期
     */
    private LocalDate startDate;
    /**
     * 离班日期
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDate endDate;

}
