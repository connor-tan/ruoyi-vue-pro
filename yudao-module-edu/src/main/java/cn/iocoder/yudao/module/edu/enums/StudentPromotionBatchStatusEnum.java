package cn.iocoder.yudao.module.edu.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 学生升班批次状态。
 */
@Getter
@AllArgsConstructor
public enum StudentPromotionBatchStatusEnum implements ArrayValuable<Integer> {

    SUCCESS(1, "成功"),
    SKIPPED(2, "跳过"),
    FAILED(3, "失败"),
    ROLLED_BACK(4, "已回滚");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(StudentPromotionBatchStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
