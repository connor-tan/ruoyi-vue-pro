package cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "管理后台 - 学生流转分页 Request VO")
@Data
public class StudentFlowPageReqVO extends PageParam {

    @Schema(description = "任务ID", example = "1")
    private Long taskId;

    @Schema(description = "批次ID", example = "10")
    private Long batchId;

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "流转类型", example = "PROMOTE")
    private String changeType;

    @Schema(description = "生效日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate[] effectiveDate;

}
