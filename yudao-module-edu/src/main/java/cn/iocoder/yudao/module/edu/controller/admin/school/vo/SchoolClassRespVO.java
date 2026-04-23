package cn.iocoder.yudao.module.edu.controller.admin.school.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Schema(description = "管理后台 - 班级 Response VO")
@Data
public class SchoolClassRespVO {

    @Schema(description = "班级编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "学校编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long schoolId;

    @Schema(description = "入学批次", requiredMode = Schema.RequiredMode.REQUIRED, example = "2023")
    private Integer entryYear;

    @Schema(description = "学校年级编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long schoolGradeId;

    @Schema(description = "学年编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long schoolYearId;

    @Schema(description = "学年名称", example = "2023-2024学年")
    private String schoolYearName;

    @Schema(description = "阶段", requiredMode = Schema.RequiredMode.REQUIRED, example = "primary")
    private String stage;

    @Schema(description = "年级标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "P1")
    private String gradeNo;

    @Schema(description = "年级名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一年级")
    private String gradeName;

    @Schema(description = "年级别名", example = "七年级")
    private String aliasName;

    @Schema(description = "班级号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer classNo;

    @Schema(description = "班级名称", example = "2023级一年级1班")
    private String className;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime createTime;

}
