package cn.iocoder.yudao.module.product.dal.dataobject.publication;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("product_publication_sku_grade_rel")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPublicationSkuGradeRelDO extends BaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long skuId;

    private Long gradeCatalogId;
}
