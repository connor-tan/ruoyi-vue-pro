package cn.iocoder.yudao.module.product.dal.dataobject.publication;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("product_publication_type")
@KeySequence("product_publication_type_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPublicationTypeDO extends BaseDO {

    @TableId
    private Long id;

    private String code;

    private String name;

    private Integer sort;

    /**
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

    private String remark;
}
