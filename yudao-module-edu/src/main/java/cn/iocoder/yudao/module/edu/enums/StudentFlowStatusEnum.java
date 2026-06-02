package cn.iocoder.yudao.module.edu.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 学生流转记录状态。
 */
@Getter
@AllArgsConstructor
public enum StudentFlowStatusEnum implements ArrayValuable<Integer> {

    ACTIVE(1, "有效"),
    ROLLED_BACK(2, "已回滚");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(StudentFlowStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
