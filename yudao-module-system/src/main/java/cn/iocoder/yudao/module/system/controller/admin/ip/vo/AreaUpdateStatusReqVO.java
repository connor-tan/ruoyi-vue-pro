package cn.iocoder.yudao.module.system.controller.admin.ip.vo;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.framework.dict.validation.InDict;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 地区更新状态 Request VO")
@Data
public class AreaUpdateStatusReqVO {

    @Schema(description = "地区编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "320505")
    @NotNull(message = "地区编号不能为空")
    private Integer id;

    @Schema(description = "状态，见 CommonStatusEnum 枚举", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    @InEnum(value = CommonStatusEnum.class, message = "修改状态必须是 {value}")
    @InDict(type = DictTypeConstants.COMMON_STATUS)
    private Integer status;

}
