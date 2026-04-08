package cn.iocoder.yudao.module.subscription.controller.admin.windowtemplate.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 订刊规则模板精简 Response VO")
@Data
public class SubscriptionWindowTemplateSimpleRespVO {

    private Long id;

    private String code;

    private String name;

    private String targetPeriod;

    private String gradeCalcRule;

    private String description;

    private Integer status;

    private Boolean builtIn;
}
