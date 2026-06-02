package cn.iocoder.yudao.module.edu.controller.admin.student.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.edu.enums.StudentStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 学生分页 Request VO")
@Data
public class StudentPageReqVO extends PageParam {

    @Schema(description = "姓名", example = "芋艿")
    private String studentName;

    @Schema(description = "家长")
    private Long belongTo;

    @Schema(description = "学校", example = "26463")
    private Long currentSchoolId;

    @Schema(description = "当前班级", example = "1024")
    private Long currentClassId;

    @Schema(description = "入学年")
    private Integer entryYear;

    @Schema(description = "学号")
    private Integer studentCode;

    @Schema(description = "状态（1-在读，2-毕业，3-休学，4-待升学，5-待入学）", example = "1")
    @InEnum(value = StudentStatusEnum.class, message = "状态必须是 {value}")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
