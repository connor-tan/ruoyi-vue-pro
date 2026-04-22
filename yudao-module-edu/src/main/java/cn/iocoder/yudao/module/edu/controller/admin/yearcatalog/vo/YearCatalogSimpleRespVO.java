package cn.iocoder.yudao.module.edu.controller.admin.yearcatalog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 学年目录精简 Response VO")
@Data
public class YearCatalogSimpleRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "开始年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026")
    private Integer yearStart;

    @Schema(description = "结束年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2027")
    private Integer yearEnd;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-2027学年")
    private String name;
}
