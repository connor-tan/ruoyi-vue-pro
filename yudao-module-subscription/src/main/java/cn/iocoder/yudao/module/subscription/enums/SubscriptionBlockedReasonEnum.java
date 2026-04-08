package cn.iocoder.yudao.module.subscription.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SubscriptionBlockedReasonEnum implements ArrayValuable<String> {

    WINDOW_NOT_OPEN("WINDOW_NOT_OPEN", "订刊窗口未开放"),
    PENDING_ADVANCE_BIND_REQUIRED("PENDING_ADVANCE_BIND_REQUIRED", "待升学学生需先完成新学校绑定"),
    STUDENT_STATUS_UNSUPPORTED("STUDENT_STATUS_UNSUPPORTED", "当前学生状态不可订刊"),
    NO_CURRENT_CLASS("NO_CURRENT_CLASS", "当前学生不存在有效班级信息"),
    TERMINAL_GRADE_PROMOTION_UNSUPPORTED("TERMINAL_GRADE_PROMOTION_UNSUPPORTED", "当前年级处于末级，需等待后续升学绑定流程"),
    SCHOOL_GRADE_NOT_EXISTS("SCHOOL_GRADE_NOT_EXISTS", "当前学生缺少有效的学校年级定义");

    public static final String[] ARRAYS = new String[] {
            "WINDOW_NOT_OPEN", "PENDING_ADVANCE_BIND_REQUIRED", "STUDENT_STATUS_UNSUPPORTED",
            "NO_CURRENT_CLASS", "TERMINAL_GRADE_PROMOTION_UNSUPPORTED", "SCHOOL_GRADE_NOT_EXISTS"
    };

    private final String reason;

    private final String description;

    @Override
    public String[] array() {
        return ARRAYS;
    }
}
