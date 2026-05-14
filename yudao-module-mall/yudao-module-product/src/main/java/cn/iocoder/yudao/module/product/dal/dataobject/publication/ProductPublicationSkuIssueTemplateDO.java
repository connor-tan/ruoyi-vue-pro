package cn.iocoder.yudao.module.product.dal.dataobject.publication;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("product_publication_sku_issue_template")
@KeySequence("product_publication_sku_issue_template_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPublicationSkuIssueTemplateDO extends BaseDO {

    @TableId
    private Long id;

    private Long skuId;

    private Integer issueNo;

    private String issueName;

    private Integer publishOffsetDays;

    private Integer deliveryOffsetDays;

    private Integer sort;

    private Integer status;

    private String remark;

}
