package cn.iocoder.yudao.module.infra.controller.admin.demo.demo03.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 学生班级 Response VO")
@Data
public class Demo03GradeRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "学生编号", example = "1")
    private Long studentId;

    @Schema(description = "名字", example = "一班")
    private String name;

    @Schema(description = "班主任", example = "张老师")
    private String teacher;

}
