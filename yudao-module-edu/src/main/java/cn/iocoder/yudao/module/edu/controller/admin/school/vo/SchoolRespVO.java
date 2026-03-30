package cn.iocoder.yudao.module.edu.controller.admin.school.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

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

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
