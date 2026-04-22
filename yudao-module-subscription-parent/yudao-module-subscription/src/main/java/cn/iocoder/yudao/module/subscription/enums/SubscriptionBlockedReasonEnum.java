package cn.iocoder.yudao.module.subscription.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SubscriptionBlockedReasonEnum implements ArrayValuable<String> {

    WINDOW_NOT_OPEN("WINDOW_NOT_OPEN", "订刊窗口未开放"),
    PENDING_ADVANCE_BIND_REQUIRED("PENDING_ADVANCE_BIND_REQUIRED", "待升学学生需先完成新学校绑定"),
    TARGET_SCHOOL_YEAR_NOT_CONFIGURED("TARGET_SCHOOL_YEAR_NOT_CONFIGURED", "学生所在学校尚未配置目标学年"),
    FUTURE_CLASS_BIND_REQUIRED("FUTURE_CLASS_BIND_REQUIRED", "预售学生需先完成目标学年班级绑定"),
    MULTI_FUTURE_CLASS("MULTI_FUTURE_CLASS", "当前学生存在多条目标学年班级绑定"),
    STUDENT_STATUS_UNSUPPORTED("STUDENT_STATUS_UNSUPPORTED", "当前学生状态不可订刊"),
    NO_CURRENT_CLASS("NO_CURRENT_CLASS", "当前学生不存在有效班级信息"),
    TERMINAL_GRADE_PROMOTION_UNSUPPORTED("TERMINAL_GRADE_PROMOTION_UNSUPPORTED", "当前年级处于末级，需等待后续升学绑定流程"),
    NEXT_GRADE_NOT_ENABLED("NEXT_GRADE_NOT_ENABLED", "学校未启用连续的下一年级"),
    SCHOOL_GRADE_NOT_EXISTS("SCHOOL_GRADE_NOT_EXISTS", "当前学生缺少有效的学校年级定义");

    public static final String[] ARRAYS = new String[] {
            "WINDOW_NOT_OPEN", "PENDING_ADVANCE_BIND_REQUIRED", "TARGET_SCHOOL_YEAR_NOT_CONFIGURED",
            "FUTURE_CLASS_BIND_REQUIRED", "MULTI_FUTURE_CLASS",
            "STUDENT_STATUS_UNSUPPORTED",
            "NO_CURRENT_CLASS", "TERMINAL_GRADE_PROMOTION_UNSUPPORTED", "NEXT_GRADE_NOT_ENABLED",
            "SCHOOL_GRADE_NOT_EXISTS"
    };

    private final String reason;

    private final String description;

    @Override
    public String[] array() {
        return ARRAYS;
    }
}
