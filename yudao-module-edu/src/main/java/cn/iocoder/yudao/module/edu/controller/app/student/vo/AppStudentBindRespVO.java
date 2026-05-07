package cn.iocoder.yudao.module.edu.controller.app.student.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 绑定学生 Response VO")
@Data
public class AppStudentBindRespVO {

    @Schema(description = "绑定结果：CREATED、BOUND、CONFIRM_REQUIRED", requiredMode = Schema.RequiredMode.REQUIRED)
    private String result;

    @Schema(description = "学生编号", example = "1")
    private Long studentId;

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "冲突类型：GRADE_MISMATCH、CLASS_MISMATCH、CLASS_MISSING", example = "GRADE_MISMATCH")
    private String conflictType;

    @Schema(description = "提示文案")
    private String message;

    @Schema(description = "系统当前学校年级编号", example = "1")
    private Long currentSchoolGradeId;

    @Schema(description = "系统当前年级目录编号", example = "1")
    private Long currentGradeCatalogId;

    @Schema(description = "系统当前年级名称", example = "一年级")
    private String currentGradeName;

    @Schema(description = "系统当前班级编号", example = "1")
    private Long currentClassId;

    @Schema(description = "系统当前班级名称", example = "2025级一年级1班")
    private String currentClassName;

    @Schema(description = "用户选择学校年级编号", example = "2")
    private Long selectedSchoolGradeId;

    @Schema(description = "用户选择年级目录编号", example = "2")
    private Long selectedGradeCatalogId;

    @Schema(description = "用户选择年级名称", example = "二年级")
    private String selectedGradeName;

    @Schema(description = "用户选择班级编号", example = "2")
    private Long selectedClassId;

    @Schema(description = "用户选择班级名称", example = "2025级二年级1班")
    private String selectedClassName;

}
