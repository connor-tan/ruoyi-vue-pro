package cn.iocoder.yudao.module.edu.dal.dataobject.school;

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
 * 学校办学学段 DO
 */
@TableName("edu_school_stage")
@KeySequence("edu_school_stage_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolStageDO extends BaseDO {

    /**
     * 记录编号
     */
    @TableId
    private Long id;
    /**
     * 学校编号
     */
    private Long schoolId;
    /**
     * 办学学段，对齐 edu_grade_catalog.stage / edu_stage 字典
     */
    private String stage;

}
