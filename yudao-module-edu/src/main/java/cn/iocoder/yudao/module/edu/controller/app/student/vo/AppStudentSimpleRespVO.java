package cn.iocoder.yudao.module.edu.controller.app.student.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 学生精简 Response VO")
@Data
public class AppStudentSimpleRespVO {

    @Schema(description = "学生编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "学生姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String studentName;

    @Schema(description = "当前学校编号", example = "1")
    private Long currentSchoolId;

    @Schema(description = "当前学校名称", example = "实验小学")
    private String currentSchoolName;

    @Schema(description = "当前年级名称", example = "一年级")
    private String gradeName;

    @Schema(description = "当前班级名称", example = "2025级一年级1班")
    private String className;

    @Schema(description = "学生状态", example = "1")
    private Integer status;
}
