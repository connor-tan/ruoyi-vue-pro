package cn.iocoder.yudao.module.repo.dal.dataobject.warehouse;

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

import java.math.BigDecimal;

/**
 * 仓库 DO
 */
@TableName("repo_warehouse")
@KeySequence("repo_warehouse_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepoWarehouseDO extends BaseDO {

    @TableId
    private Long id;

    private String name;

    private String address;

    private Long sort;

    private String remark;

    private String principal;

    private BigDecimal warehousePrice;

    private BigDecimal truckagePrice;

    /**
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;

    private Boolean defaultStatus;

}
