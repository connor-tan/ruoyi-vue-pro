package cn.iocoder.yudao.module.edu.controller.app.student.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.edu.enums.AppStudentBindModeEnum;
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

    @Schema(description = "绑定场景：CURRENT_READING-已在读，FUTURE_ENTRY-即将入学",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "CURRENT_READING")
    @NotNull(message = "绑定场景不能为空")
    @InEnum(value = AppStudentBindModeEnum.class, message = "绑定场景必须是 {value}")
    private String bindMode;

    @Schema(description = "学校学年编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "学年不能为空")
    private Long schoolYearId;

    @Schema(description = "学校年级编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "年级不能为空")
    private Long schoolGradeId;

    @Schema(description = "班级编号；选择已存在班级时传入，待创建班级可为空", example = "1")
    private Long classId;

    @Schema(description = "班级号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "班级号不能为空")
    private Integer classNo;

    @Schema(description = "学生姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "学生姓名不能为空")
    private String studentName;

    @Schema(description = "是否确认强制修改班级", example = "false")
    private Boolean forceUpdate;

}
