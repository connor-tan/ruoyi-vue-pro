package cn.iocoder.yudao.module.edu.controller.app.student.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "用户 App - 绑定学生 Request VO")
@Data
public class AppStudentBindReqVO {

    @Schema(description = "学校编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "学校不能为空")
    private Long schoolId;

    @Schema(description = "学校年级编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "年级不能为空")
    private Long schoolGradeId;

    @Schema(description = "班级编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "班级不能为空")
    private Long classId;

    @Schema(description = "学生姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "学生姓名不能为空")
    private String studentName;

    @Schema(description = "是否确认强制修改班级", example = "false")
    private Boolean forceUpdate;

}
