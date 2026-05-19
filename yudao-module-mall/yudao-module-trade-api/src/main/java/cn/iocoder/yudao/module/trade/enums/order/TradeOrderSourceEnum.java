package cn.iocoder.yudao.module.trade.enums.order;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum TradeOrderSourceEnum implements ArrayValuable<String> {

    APP("APP", "APP 下单"),
    ADMIN_MANUAL("ADMIN_MANUAL", "后台手动新建"),
    ADMIN_IMPORT("ADMIN_IMPORT", "后台批量导入"),
    ADMIN_ONLINE("ADMIN_ONLINE", "后台在线下单");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(TradeOrderSourceEnum::getSource).toArray(String[]::new);

    private final String source;

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static boolean isAdmin(String source) {
        return ADMIN_MANUAL.getSource().equals(source) || ADMIN_IMPORT.getSource().equals(source);
    }

    public static boolean isAdminOnline(String source) {
        return ADMIN_ONLINE.getSource().equals(source);
    }

}
