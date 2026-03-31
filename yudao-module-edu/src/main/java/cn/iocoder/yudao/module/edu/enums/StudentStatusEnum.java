package cn.iocoder.yudao.module.edu.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 学生状态枚举
 */
@Getter
@AllArgsConstructor
public enum StudentStatusEnum implements ArrayValuable<Integer> {

    READING(1, "在读"),
    GRADUATED(2, "毕业"),
    SUSPENDED(3, "休学"),
    PENDING_ADVANCE(4, "待升学");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(StudentStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
