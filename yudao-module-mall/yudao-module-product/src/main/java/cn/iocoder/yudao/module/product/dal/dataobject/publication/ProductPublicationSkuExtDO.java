package cn.iocoder.yudao.module.product.dal.dataobject.publication;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("product_publication_sku_ext")
@KeySequence("product_publication_sku_ext_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPublicationSkuExtDO extends BaseDO {

    @TableId
    private Long skuId;

    private String isbn;

    private String remark;
}
