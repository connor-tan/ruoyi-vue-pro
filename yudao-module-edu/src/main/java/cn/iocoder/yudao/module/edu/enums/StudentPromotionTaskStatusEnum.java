package cn.iocoder.yudao.module.edu.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 学生全局升班任务状态。
 */
@Getter
@AllArgsConstructor
public enum StudentPromotionTaskStatusEnum implements ArrayValuable<Integer> {

    RUNNING(0, "执行中"),
    SUCCESS(1, "成功"),
    PARTIAL(2, "部分成功"),
    FAILED(3, "失败"),
    ROLLED_BACK(4, "已回滚");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(StudentPromotionTaskStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
