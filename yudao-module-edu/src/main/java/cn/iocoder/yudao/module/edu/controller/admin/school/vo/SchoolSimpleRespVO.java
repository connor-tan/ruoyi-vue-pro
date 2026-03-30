package cn.iocoder.yudao.module.edu.controller.admin.school.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 学校精简 Response VO")
@Data
public class SchoolSimpleRespVO {

    @Schema(description = "学校编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "学校名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "无锡市实验小学")
    private String schoolName;

}
