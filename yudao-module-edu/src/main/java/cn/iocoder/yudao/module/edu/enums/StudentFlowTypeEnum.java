package cn.iocoder.yudao.module.edu.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 学生流转类型。
 */
@Getter
@AllArgsConstructor
public enum StudentFlowTypeEnum implements ArrayValuable<String> {

    ENROLL("ENROLL", "入学"),
    PROMOTE("PROMOTE", "升班"),
    TRANSFER("TRANSFER", "转班"),
    REPEAT("REPEAT", "留级"),
    GRADUATE("GRADUATE", "毕业"),
    PENDING_ADVANCE("PENDING_ADVANCE", "待升学");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(StudentFlowTypeEnum::getType)
            .toArray(String[]::new);

    private final String type;
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
