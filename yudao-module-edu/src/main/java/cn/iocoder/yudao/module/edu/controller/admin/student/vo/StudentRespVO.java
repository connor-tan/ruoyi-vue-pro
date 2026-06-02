package cn.iocoder.yudao.module.edu.controller.admin.student.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Schema(description = "管理后台 - 学生 Response VO")
@Data
@ExcelIgnoreUnannotated
public class StudentRespVO {

    @Schema(description = "学生ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "19423")
    @ExcelProperty("学生ID")
    private Long id;

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @ExcelProperty("姓名")
    private String studentName;

    @Schema(description = "家长", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("家长")
    private Long belongTo;

    @Schema(description = "家长昵称", example = "小王妈妈")
    @ExcelProperty("家长昵称")
    private String parentNickname;

    @Schema(description = "家长手机号", example = "13800138000")
    @ExcelProperty("家长手机号")
    private String parentMobile;

    @Schema(description = "学校", requiredMode = Schema.RequiredMode.REQUIRED, example = "26463")
    @ExcelProperty("学校")
    private Long currentSchoolId;

    @Schema(description = "学校名称", example = "无锡市实验小学")
    @ExcelProperty("学校名称")
    private String currentSchoolName;

    @Schema(description = "当前班级", example = "1024")
    @ExcelProperty("当前班级ID")
    private Long currentClassId;

    @Schema(description = "当前班级名称", example = "2026级一年级1班")
    @ExcelProperty("当前班级名称")
    private String currentClassName;

    @Schema(description = "入学年", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("入学年")
    private Integer entryYear;

    @Schema(description = "学号")
    @ExcelProperty("学号")
    private Integer studentCode;

    @Schema(description = "状态（1-在读，2-毕业，3-休学，4-待升学，5-待入学）", example = "1")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime createTime;

}
