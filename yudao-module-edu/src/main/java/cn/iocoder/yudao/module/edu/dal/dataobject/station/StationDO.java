package cn.iocoder.yudao.module.edu.dal.dataobject.station;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.edu.enums.DictTypeConstants;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@TableName("edu_station")
@KeySequence("edu_station_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationDO extends BaseDO {

    @TableId
    private Long id;

    private String stationName;

    private Long areaId;

    private String contactName;

    private String contactMobile;

    private String stationAddress;

    private Integer sort;

    /**
     * 枚举 {@link CommonStatusEnum}
     * 字典 {@link DictTypeConstants#EDU_COMMON_STATUS}
     */
    private Integer status;

    private String remark;

    @TableField(exist = false)
    private Long schoolCount;
}
