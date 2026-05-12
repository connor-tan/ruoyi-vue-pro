package cn.iocoder.yudao.module.publication.api.enums;

/**
 * 刊物期次模式。
 */
public enum PublicationIssueModeEnum {

    SINGLE("SINGLE", "独立刊物"),
    PERIODICAL("PERIODICAL", "期刊");

    private final String code;
    private final String name;

    PublicationIssueModeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String normalize(String code) {
        if (code == null || code.isBlank()) {
            return SINGLE.getCode();
        }
        for (PublicationIssueModeEnum item : values()) {
            if (item.code.equalsIgnoreCase(code)) {
                return item.code;
            }
        }
        return SINGLE.getCode();
    }

    public static boolean isValid(String code) {
        if (code == null || code.isBlank()) {
            return true;
        }
        for (PublicationIssueModeEnum item : values()) {
            if (item.code.equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPeriodical(String code) {
        return PERIODICAL.getCode().equals(normalize(code));
    }

    public static boolean isSingle(String code) {
        return SINGLE.getCode().equals(normalize(code));
    }

}
