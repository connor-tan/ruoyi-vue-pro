package cn.iocoder.yudao.module.edu.controller.admin.yearcatalog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 学年目录新增/修改 Request VO")
@Data
public class YearCatalogSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "开始年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026")
    @NotNull(message = "开始年份不能为空")
    private Integer yearStart;

    @Schema(description = "结束年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2027")
    @NotNull(message = "结束年份不能为空")
    private Integer yearEnd;
}
