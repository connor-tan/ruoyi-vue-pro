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
 * 年级目录 DO
 */
@TableName("edu_grade_catalog")
@KeySequence("edu_grade_catalog_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeCatalogDO extends BaseDO {

    /**
     * 目录编号
     */
    @TableId
    private Long id;
    /**
     * 阶段
     */
    private String stage;
    /**
     * 年级标识
     */
    private String gradeNo;
    /**
     * 年级名称
     */
    private String gradeName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 状态
     */
    private Integer status;

}
