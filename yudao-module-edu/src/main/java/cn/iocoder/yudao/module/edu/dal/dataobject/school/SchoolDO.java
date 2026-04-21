package cn.iocoder.yudao.module.edu.dal.dataobject.school;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
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

import java.util.List;

/**
 * 学校信息 DO
 *
 * @author 芋道源码
 */
@TableName("edu_school")
@KeySequence("edu_school_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolDO extends BaseDO {

    /**
     * 学校ID
     */
    @TableId
    private Long id;
    /**
     * 学校名称
     */
    private String schoolName;
    /**
     * 区域ID
     */
    private Long areaId;
    /**
     * 学校地址
     */
    private String schoolAddress;
    /**
     * 归属站点 ID
     */
    private Long stationId;
    /**
     * 学校代码
     */
    private String code;

    /**
     * 办学学段编码列表，对齐 edu_grade_catalog.stage / edu_stage 字典
     */
    @TableField(exist = false)
    private List<String> stageCodes;

    @TableField(exist = false)
    private String stationName;

}
