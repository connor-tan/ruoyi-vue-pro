package cn.iocoder.yudao.module.subscription.dal.dataobject.window;

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

@TableName("sub_window_sku")
@KeySequence("sub_window_sku_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionWindowSkuDO extends BaseDO {

    @TableId
    private Long id;

    private Long windowSpuId;

    private Long productSkuId;

    private Integer status;

    private Integer sort;

    private Integer maxQuantityPerStudent;

    private String remark;
}
