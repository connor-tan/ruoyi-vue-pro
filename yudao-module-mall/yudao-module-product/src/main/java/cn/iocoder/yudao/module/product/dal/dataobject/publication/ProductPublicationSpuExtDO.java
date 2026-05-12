package cn.iocoder.yudao.module.product.dal.dataobject.publication;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.publication.api.enums.PublicationIssueModeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("product_publication_spu_ext")
@KeySequence("product_publication_spu_ext_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPublicationSpuExtDO extends BaseDO {

    @TableId
    private Long spuId;

    private Long publisherId;

    private Long publicationTypeId;

    /**
     * 期次模式
     *
     * 枚举 {@link PublicationIssueModeEnum}
     */
    private String issueMode;

    private String issueCycle;

    private String issn;

    private String cnCode;

    private String postDistributionCode;
}
