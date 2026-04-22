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
 * 全局学年目录 DO
 */
@TableName("edu_year_catalog")
@KeySequence("edu_year_catalog_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YearCatalogDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 学年开始年份
     */
    private Integer yearStart;

    /**
     * 学年结束年份
     */
    private Integer yearEnd;
}
