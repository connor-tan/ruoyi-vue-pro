package cn.iocoder.yudao.module.product.enums.publication;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductDomainTypeEnum {

    NORMAL("NORMAL", "普通商品"),
    PUBLICATION("PUBLICATION", "刊物商品");

    private final String code;
    private final String name;

    public static boolean isPublication(String code) {
        return PUBLICATION.code.equals(code);
    }
}
