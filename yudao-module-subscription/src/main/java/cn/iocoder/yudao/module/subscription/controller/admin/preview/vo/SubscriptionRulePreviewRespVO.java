package cn.iocoder.yudao.module.subscription.controller.admin.preview.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 订刊规则预览 Response VO")
@Data
public class SubscriptionRulePreviewRespVO {

    private Long studentId;

    private String studentName;

    private Long schoolId;

    private String schoolName;

    private Long effectiveGradeCatalogId;

    private String effectiveGradeNo;

    private String effectiveGradeName;

    private String effectiveGradeAliasName;

    private String blockedReason;

    private List<SubscriptionRulePreviewPublicationRespVO> publications;
}
