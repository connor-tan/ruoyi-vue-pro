package cn.iocoder.yudao.module.edu.controller.admin.school.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Schema(description = "管理后台 - 学校信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class SchoolRespVO {

    @Schema(description = "学校ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "31522")
    @ExcelProperty("学校ID")
    private Long id;

    @Schema(description = "学校名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("学校名称")
    private String schoolName;

    @Schema(description = "区域ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "68")
    @ExcelProperty("区域ID")
    private Long areaId;

    @Schema(description = "地区名称", example = "江苏省 无锡市 新吴区")
    @ExcelProperty("地区名称")
    private String areaName;

    @Schema(description = "学校地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("学校地址")
    private String schoolAddress;

    @Schema(description = "学校代码")
    @ExcelProperty("学校代码")
    private String code;

    @Schema(description = "办学学段编码列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[\"primary\",\"middle\"]")
    private List<String> stageCodes;

    @Schema(description = "办学学段名称", example = "小学、初中")
    @ExcelProperty("办学学段")
    private String stageNames;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime createTime;

}
