package cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 学生批量升班学年选项 Response VO")
@Data
public class StudentPromotionYearOptionRespVO {

    @Schema(description = "学年开始年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2025")
    private Integer yearStart;

    @Schema(description = "学年结束年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026")
    private Integer yearEnd;

    @Schema(description = "学年名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "2025-2026学年")
    private String name;

}
