package cn.iocoder.yudao.module.edu.controller.admin.school.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 学校信息新增/修改 Request VO")
@Data
public class SchoolSaveReqVO {

    @Schema(description = "学校ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "31522")
    private Long id;

    @Schema(description = "学校名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotEmpty(message = "学校名称不能为空")
    private String schoolName;

    @Schema(description = "区域ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "68")
    @NotNull(message = "区域ID不能为空")
    private Long areaId;

    @Schema(description = "学校地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "学校地址不能为空")
    private String schoolAddress;

    @Schema(description = "归属站点编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "归属站点不能为空")
    private Long stationId;

    @Schema(description = "学校代码")
    private String code;

    @Schema(description = "办学学段编码列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[\"primary\",\"middle\"]")
    @NotEmpty(message = "办学学段不能为空")
    private List<String> stageCodes;

}
