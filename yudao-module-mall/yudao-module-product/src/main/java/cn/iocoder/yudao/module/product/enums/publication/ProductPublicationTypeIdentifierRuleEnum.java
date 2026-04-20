package cn.iocoder.yudao.module.product.enums.publication;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductPublicationTypeIdentifierRuleEnum implements ArrayValuable<String> {

    NONE("NONE", "不要求标识"),
    TITLE_PERIODICAL_IDENTIFIER_REQUIRED("TITLE_PERIODICAL_IDENTIFIER_REQUIRED", "主档需填写 ISSN/CN 刊号/邮发代号"),
    SKU_ISBN_REQUIRED("SKU_ISBN_REQUIRED", "SKU 需填写 ISBN");

    public static final String[] ARRAYS = new String[] {
            "NONE",
            "TITLE_PERIODICAL_IDENTIFIER_REQUIRED",
            "SKU_ISBN_REQUIRED"
    };

    private final String rule;

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static String defaultRule() {
        return NONE.rule;
    }

    public static String normalize(String rule) {
        for (ProductPublicationTypeIdentifierRuleEnum value : values()) {
            if (value.rule.equals(rule)) {
                return value.rule;
            }
        }
        return defaultRule();
    }

    public static boolean requiresTitleIdentifier(String rule) {
        return TITLE_PERIODICAL_IDENTIFIER_REQUIRED.rule.equals(normalize(rule));
    }

    public static boolean requiresSkuIsbn(String rule) {
        return SKU_ISBN_REQUIRED.rule.equals(normalize(rule));
    }
}
