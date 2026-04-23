package cn.iocoder.yudao.module.publication.api.enums;

import java.util.Arrays;

public enum PublicationIdentifierRuleEnum {

    NONE("NONE", "不要求标识"),
    TITLE_PERIODICAL_IDENTIFIER_REQUIRED("TITLE_PERIODICAL_IDENTIFIER_REQUIRED", "刊物需填写 ISSN/CN/邮发代号"),
    SKU_ISBN_REQUIRED("SKU_ISBN_REQUIRED", "SKU 需填写 ISBN");

    private final String code;
    private final String name;

    PublicationIdentifierRuleEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String defaultCode() {
        return NONE.code;
    }

    public static String normalize(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code))
                .findFirst()
                .map(PublicationIdentifierRuleEnum::getCode)
                .orElse(NONE.code);
    }

    public static boolean requiresTitleIdentifier(String code) {
        return TITLE_PERIODICAL_IDENTIFIER_REQUIRED.code.equals(normalize(code));
    }

    public static boolean requiresSkuIsbn(String code) {
        return SKU_ISBN_REQUIRED.code.equals(normalize(code));
    }
}
