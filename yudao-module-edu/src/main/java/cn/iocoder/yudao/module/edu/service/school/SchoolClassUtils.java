package cn.iocoder.yudao.module.edu.service.school;

import cn.hutool.core.util.StrUtil;

/**
 * 班级工具类
 */
public final class SchoolClassUtils {

    private SchoolClassUtils() {
    }

    public static String buildClassName(Integer entryYear, String gradeName, Integer classNo) {
        return StrUtil.format("{}级{}{}班", entryYear, StrUtil.blankToDefault(gradeName, ""), classNo);
    }

}
