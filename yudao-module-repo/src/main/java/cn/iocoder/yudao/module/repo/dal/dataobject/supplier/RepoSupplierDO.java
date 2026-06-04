package cn.iocoder.yudao.module.repo.dal.dataobject.supplier;

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
 * 仓库供应商 DO。
 */
@TableName("repo_supplier")
@KeySequence("repo_supplier_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepoSupplierDO extends BaseDO {

    @TableId
    private Long id;

    private String name;

    private String code;

    private String contactName;

    private String contactMobile;

    private String address;

    private Long sort;

    /**
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;

    private String remark;

}
