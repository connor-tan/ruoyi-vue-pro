package cn.iocoder.yudao.module.edu.controller.app.school.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 学校精简 Response VO")
@Data
public class AppSchoolSimpleRespVO {

    @Schema(description = "学校编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "学校名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "实验小学")
    private String schoolName;

    @Schema(description = "地区编号", example = "320214")
    private Long areaId;

}
