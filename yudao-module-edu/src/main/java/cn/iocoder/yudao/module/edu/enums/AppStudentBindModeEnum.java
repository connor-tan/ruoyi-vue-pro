package cn.iocoder.yudao.module.edu.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * App 家长绑定学生场景。
 */
@Getter
@AllArgsConstructor
public enum AppStudentBindModeEnum implements ArrayValuable<String> {

    CURRENT_READING("CURRENT_READING", "已在读"),
    FUTURE_ENTRY("FUTURE_ENTRY", "即将入学");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(AppStudentBindModeEnum::getMode)
            .toArray(String[]::new);

    private final String mode;
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
