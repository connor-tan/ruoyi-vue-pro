package cn.iocoder.yudao.module.promotion.dal.mysql.coupon;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.promotion.dal.dataobject.coupon.CouponTemplateDO;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionDiscountTypeEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionProductScopeEnum;
import cn.iocoder.yudao.module.promotion.enums.coupon.CouponTakeTypeEnum;
import cn.iocoder.yudao.module.promotion.enums.coupon.CouponTemplateValidityTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CouponTemplateMapperMultiCategoryTest extends BaseDbUnitTest {

    @Resource
    private CouponTemplateMapper couponTemplateMapper;
    @Resource
    private DataSource dataSource;

    @Test
    void selectListBySpu_matchesAllSpuAndParentCategoryScopes() {
        new JdbcTemplate(dataSource).execute("CREATE ALIAS IF NOT EXISTS FIND_IN_SET FOR \"" +
                CouponTemplateMapperMultiCategoryTest.class.getName() + ".findInSet\"");

        createTemplate("all", PromotionProductScopeEnum.ALL.getScope(), null);
        createTemplate("spu", PromotionProductScopeEnum.SPU.getScope(), List.of(10L));
        createTemplate("parent", PromotionProductScopeEnum.CATEGORY.getScope(), List.of(100L));
        createTemplate("unmatched", PromotionProductScopeEnum.CATEGORY.getScope(), List.of(999L));

        List<CouponTemplateDO> templates = couponTemplateMapper.selectListBySpu(
                null, 10L, List.of(101L, 100L), 10);

        assertEquals(3, templates.size());
    }

    private void createTemplate(String name, Integer productScope, List<Long> productScopeValues) {
        CouponTemplateDO template = new CouponTemplateDO();
        template.setName(name);
        template.setStatus(CommonStatusEnum.ENABLE.getStatus());
        template.setTotalCount(CouponTemplateDO.TOTAL_COUNT_MAX);
        template.setTakeLimitCount(CouponTemplateDO.TAKE_LIMIT_COUNT_MAX);
        template.setTakeType(CouponTakeTypeEnum.USER.getType());
        template.setUsePrice(0);
        template.setProductScope(productScope);
        template.setProductScopeValues(productScopeValues);
        template.setValidityType(CouponTemplateValidityTypeEnum.DATE.getType());
        template.setValidStartTime(LocalDateTime.now().minusDays(1));
        template.setValidEndTime(LocalDateTime.now().plusDays(1));
        template.setDiscountType(PromotionDiscountTypeEnum.PRICE.getType());
        template.setDiscountPrice(100);
        template.setTakeCount(0);
        template.setUseCount(0);
        couponTemplateMapper.insert(template);
    }

    public static int findInSet(long value, String values) {
        if (values == null || values.isBlank()) {
            return 0;
        }
        String target = String.valueOf(value);
        return Arrays.stream(values.split(",")).map(String::trim)
                .anyMatch(item -> item.equals(target)) ? 1 : 0;
    }

}
