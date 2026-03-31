package cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 学生全局批量升班预览 Request VO")
@Data
public class StudentGlobalPromotionPreviewReqVO {

    @Schema(description = "来源学年开始年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2025")
    @NotNull(message = "来源学年不能为空")
    private Integer fromYearStart;

    @Schema(description = "目标学年开始年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026")
    @NotNull(message = "目标学年不能为空")
    private Integer toYearStart;

    @Schema(description = "学校范围类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "ALL")
    @NotBlank(message = "学校范围类型不能为空")
    private String scopeType;

    @Schema(description = "学校编号列表", example = "[1, 2]")
    private List<Long> schoolIds;

    @Schema(description = "地区编号", example = "320200")
    private Long areaId;

    @Schema(description = "是否自动创建缺失目标班级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否自动创建目标班级不能为空")
    private Boolean autoCreateClass;

    @Schema(description = "末级学生是否自动转待升学", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "末级学生是否自动转待升学不能为空")
    private Boolean graduateTerminalStudent;

    @Schema(description = "备注", example = "2026年度全局升班")
    private String remark;

    @Schema(description = "逐人调整配置")
    @Valid
    private List<StudentPromotionAdjustmentReqVO> adjustments;

}
