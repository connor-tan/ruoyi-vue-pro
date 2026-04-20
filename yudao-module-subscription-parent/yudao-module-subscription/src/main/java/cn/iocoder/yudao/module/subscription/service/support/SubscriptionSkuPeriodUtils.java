package cn.iocoder.yudao.module.subscription.service.support;

import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSkuPublicationDO;
import cn.iocoder.yudao.module.publication.enums.PublicationTargetPeriodEnum;

import java.util.Objects;

public final class SubscriptionSkuPeriodUtils {

    private SubscriptionSkuPeriodUtils() {
    }

    public static String normalizeWindowTargetPeriod(String targetPeriod) {
        return PublicationTargetPeriodEnum.normalize(targetPeriod);
    }

    public static String normalizeSkuTargetPeriod(ProductSkuPublicationDO skuPublication) {
        return skuPublication == null
                ? PublicationTargetPeriodEnum.defaultPeriod()
                : normalizeWindowTargetPeriod(skuPublication.getTargetPeriod());
    }

    public static boolean isMatched(ProductSkuPublicationDO skuPublication, String windowTargetPeriod) {
        return Objects.equals(normalizeSkuTargetPeriod(skuPublication), normalizeWindowTargetPeriod(windowTargetPeriod));
    }
}
