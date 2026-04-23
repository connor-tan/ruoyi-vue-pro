package cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 学生一键升班明细 Response VO")
@Data
public class StudentPromotionItemRespVO {

    @Schema(description = "学生ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long studentId;

    @Schema(description = "学生姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String studentName;

    @Schema(description = "入学批次", requiredMode = Schema.RequiredMode.REQUIRED, example = "2023")
    private Integer entryYear;

    @Schema(description = "来源班级ID", example = "1")
    private Long fromClassId;

    @Schema(description = "来源学校年级ID", example = "1")
    private Long fromSchoolGradeId;

    @Schema(description = "来源班级名称", example = "2023级一年级1班")
    private String fromClassName;

    @Schema(description = "来源年级名称", example = "一年级")
    private String fromGradeName;

    @Schema(description = "来源年级别名", example = "七年级")
    private String fromGradeAliasName;

    @Schema(description = "目标班级ID", example = "2")
    private Long toClassId;

    @Schema(description = "目标学校年级ID", example = "2")
    private Long toSchoolGradeId;

    @Schema(description = "目标班级名称", example = "2023级二年级1班")
    private String toClassName;

    @Schema(description = "目标年级名称", example = "二年级")
    private String toGradeName;

    @Schema(description = "目标年级别名", example = "八年级")
    private String toGradeAliasName;

    @Schema(description = "目标班级是否缺失", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean targetClassMissing;

    @Schema(description = "处理动作", requiredMode = Schema.RequiredMode.REQUIRED, example = "PROMOTE")
    private String action;

    @Schema(description = "原因", example = "TARGET_CLASS_NOT_FOUND")
    private String reason;

}
