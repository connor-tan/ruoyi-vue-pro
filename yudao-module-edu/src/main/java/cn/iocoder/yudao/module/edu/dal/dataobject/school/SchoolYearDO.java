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

import java.time.LocalDate;

/**
 * 学年 DO
 *
 * @author connor
 */
@TableName("edu_school_year")
@KeySequence("edu_school_year_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolYearDO extends BaseDO {

    /**
     * 学年ID
     */
    @TableId
    private Long id;
    /**
     * 学校ID
     */
    private Long schoolId;
    /**
     * 全局学年目录ID
     */
    private Long yearCatalogId;
    /**
     * 学年开始-以年为计算单位
     */
    private Integer yearStart;
    /**
     * 学年结束-以年为计算单位
     */
    private Integer yearEnd;
    /**
     * 开学日期
     */
    private LocalDate startDate;
    /**
     * 放假日期
     */
    private LocalDate endDate;

}
