package cn.iocoder.yudao.module.edu.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 全局升班单校预览/执行状态。
 */
@Getter
@AllArgsConstructor
public enum StudentGlobalPromotionSchoolStatusEnum implements ArrayValuable<String> {

    READY("READY", "待执行"),
    SKIP("SKIP", "跳过"),
    SUCCESS("SUCCESS", "成功"),
    FAILED("FAILED", "失败");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(StudentGlobalPromotionSchoolStatusEnum::getStatus)
            .toArray(String[]::new);

    private final String status;
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
