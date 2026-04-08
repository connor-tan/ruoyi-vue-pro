package cn.iocoder.yudao.module.infra.controller.admin.demo.demo03.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 学生课程新增/修改 Request VO")
@Data
public class Demo03CourseSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "学生编号", example = "1")
    private Long studentId;

    @Schema(description = "名字", example = "数学")
    private String name;

    @Schema(description = "分数", example = "100")
    private Integer score;

}
