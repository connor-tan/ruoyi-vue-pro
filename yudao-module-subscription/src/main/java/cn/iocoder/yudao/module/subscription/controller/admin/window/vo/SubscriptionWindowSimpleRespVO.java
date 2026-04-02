package cn.iocoder.yudao.module.subscription.controller.admin.window.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 订刊窗口精简 Response VO")
@Data
public class SubscriptionWindowSimpleRespVO {

    private Long id;

    private String name;

    private Long targetSchoolYearId;

    private String targetSchoolYearName;

    private Integer targetSemester;

    private String gradeCalcRule;

    private Integer status;
}
