package cn.iocoder.yudao.module.subscription.controller.admin.rule.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SubscriptionRuleRespVO {

    private Long id;
    private Long windowId;
    private Long offerId;
    private String offerProductName;
    private String name;
    private String effectType;
    private Boolean allowGradeOverride;
    private Integer status;
    private String remark;
    private List<Condition> conditions;
    private LocalDateTime createTime;

    @Data
    public static class Condition {
        private Long id;
        private String factor;
        private String operator;
        private String value;
        private String valueName;
    }
}
