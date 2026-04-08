package cn.iocoder.yudao.module.subscription.controller.admin.windowspurule.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubscriptionWindowSpuRuleRespVO {

    private Long id;

    private Long windowSpuId;

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
