package cn.iocoder.yudao.module.edu.controller.admin.school.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 学校年级新增/修改 Request VO")
@Data
public class SchoolGradeSaveReqVO {

    @Schema(description = "学校年级编号", example = "1")
    private Long id;

    @Schema(description = "学校编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "学校ID不能为空")
    private Long schoolId;

    @Schema(description = "年级目录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "年级目录不能为空")
    private Long gradeCatalogId;

    @Schema(description = "最大班号/班级容量，0 表示暂不开放 APP 选择或自动建班",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "25")
    @NotNull(message = "最大班号不能为空")
    @Min(value = 0, message = "最大班号不能小于 0")
    @Max(value = 99, message = "最大班号不能大于 99")
    private Integer maxClassNo;

}
