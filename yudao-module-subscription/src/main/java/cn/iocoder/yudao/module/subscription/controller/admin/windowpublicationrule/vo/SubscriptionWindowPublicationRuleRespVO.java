package cn.iocoder.yudao.module.subscription.controller.admin.windowpublicationrule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 窗口刊物特殊规则 Response VO")
@Data
public class SubscriptionWindowPublicationRuleRespVO {

    private Long id;

    private Long windowPublicationId;

    private String effectType;

    private String scopeType;

    private Long schoolId;

    private String schoolName;

    private Long gradeCatalogId;

    private String gradeName;

    private String gradeAliasName;

    private Integer sort;

    private String remark;

    private LocalDateTime createTime;
}
