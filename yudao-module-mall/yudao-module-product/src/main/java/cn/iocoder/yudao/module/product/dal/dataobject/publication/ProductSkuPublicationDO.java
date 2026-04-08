package cn.iocoder.yudao.module.product.dal.dataobject.publication;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("product_sku_publication")
@KeySequence("product_sku_publication_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSkuPublicationDO extends BaseDO {

    @TableId
    private Long productSkuId;

    private String volumeLabel;

    private String editionLabel;

    private String isbn;

    private String remark;
}
