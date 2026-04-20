package cn.iocoder.yudao.module.subscription.controller.admin.window.vo;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class SubscriptionWindowEnablePrecheckRespVO {

    private Boolean pass;

    private List<String> blockers = Collections.emptyList();

    private List<String> warnings = Collections.emptyList();
}
