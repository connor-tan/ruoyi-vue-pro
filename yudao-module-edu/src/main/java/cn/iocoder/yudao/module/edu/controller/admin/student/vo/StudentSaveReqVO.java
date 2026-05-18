package cn.iocoder.yudao.module.edu.controller.admin.student.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.edu.enums.StudentStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 学生新增/修改 Request VO")
@Data
public class StudentSaveReqVO {

    @Schema(description = "学生ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "19423")
    private Long id;

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @NotEmpty(message = "姓名不能为空")
    private String studentName;

    @Schema(description = "家长", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "家长不能为空")
    private Long belongTo;

    @Schema(description = "学校", requiredMode = Schema.RequiredMode.REQUIRED, example = "26463")
    @NotNull(message = "学校不能为空")
    private Long currentSchoolId;

    @Schema(description = "入学年", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "入学年不能为空")
    private Integer entryYear;

    @Schema(description = "学号")
    private Integer studentCode;

    @Schema(description = "状态（1-在读，2-毕业，3-休学，4-待升学，5-待入学）", example = "1")
    @InEnum(value = StudentStatusEnum.class, message = "状态必须是 {value}")
    private Integer status;

    @Schema(description = "学生班级记录列表")
    @Valid
    private List<StudentClassSaveReqVO> studentClasses;

}
