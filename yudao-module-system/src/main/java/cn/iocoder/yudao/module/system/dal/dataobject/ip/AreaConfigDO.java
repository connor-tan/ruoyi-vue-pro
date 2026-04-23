package cn.iocoder.yudao.module.system.dal.dataobject.ip;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 地区配置 DO
 */
@TableName("system_area_config")
@KeySequence("system_area_config_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@TenantIgnore
public class AreaConfigDO extends BaseDO {

    /**
     * 配置编号
     */
    @TableId
    private Long id;
    /**
     * 地区编号
     */
    private Integer areaId;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
