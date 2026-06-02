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
 * 年级定义 DO
 */
@TableName("edu_school_grade")
@KeySequence("edu_school_grade_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolGradeDO extends BaseDO {

    /**
     * 年级ID
     */
    @TableId
    private Long id;
    /**
     * 学校ID
     */
    private Long schoolId;
    /**
     * 年级目录ID
     */
    private Long gradeCatalogId;
    /**
     * 最大班号/班级容量。0 表示暂不开放 APP 选择或自动建班。
     */
    private Integer maxClassNo;

}
