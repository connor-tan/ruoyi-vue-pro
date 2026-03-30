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
 * 班级 DO
 */
@TableName("edu_school_class")
@KeySequence("edu_school_class_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolClassDO extends BaseDO {

    /**
     * 班级ID
     */
    @TableId
    private Long id;
    /**
     * 学校ID
     */
    private Long schoolId;
    /**
     * 如2023届
     */
    private Integer entryYear;
    /**
     * 学校年级ID
     */
    private Long schoolGradeId;
    /**
     * 学校学年ID
     */
    private Long schoolYearId;
    /**
     * 班级号(1班、2班)
     */
    private Integer classNo;
    /**
     * 班级名称(2023级2年级1班)
     */
    private String className;

}
