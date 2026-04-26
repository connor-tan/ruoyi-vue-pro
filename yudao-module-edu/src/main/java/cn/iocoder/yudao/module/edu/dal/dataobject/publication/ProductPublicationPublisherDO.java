package cn.iocoder.yudao.module.edu.dal.dataobject.publication;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("product_publisher")
@KeySequence("product_publisher_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPublicationPublisherDO extends BaseDO {

    @TableId
    private Long id;

    private String name;

    private Integer sort;

    /**
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

    private String remark;
}
