package cn.iocoder.yudao.module.repo.dal.dataobject.supplierpublication;

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
 * 仓库侧供应商与刊物 SKU 供货关系 DO。
 */
@TableName("repo_supplier_publication_sku")
@KeySequence("repo_supplier_publication_sku_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepoSupplierPublicationSkuDO extends BaseDO {

    @TableId
    private Long id;

    private Long supplierId;

    private Long spuId;

    private Long skuId;

    private String productNameSnapshot;

    private String productSkuNameSnapshot;

    private String isbn;

    /**
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;

    private Long sort;

    private String remark;

}
