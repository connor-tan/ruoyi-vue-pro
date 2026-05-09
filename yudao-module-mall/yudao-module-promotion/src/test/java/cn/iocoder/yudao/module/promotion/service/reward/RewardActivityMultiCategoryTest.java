package cn.iocoder.yudao.module.promotion.service.reward;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.product.api.category.ProductCategoryApi;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.api.reward.dto.RewardActivityMatchRespDTO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.reward.RewardActivityDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.reward.RewardActivityMapper;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionConditionTypeEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionProductScopeEnum;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class RewardActivityMultiCategoryTest extends BaseMockitoUnitTest {

    @InjectMocks
    private RewardActivityServiceImpl rewardActivityService;
    @Mock
    private RewardActivityMapper rewardActivityMapper;
    @Mock
    private ProductCategoryApi productCategoryApi;
    @Mock
    private ProductSpuApi productSpuApi;

    @Test
    void getMatchRewardActivityListBySpuIds_matchesParentCategoryScope() {
        ProductSpuRespDTO spu = new ProductSpuRespDTO();
        spu.setId(10L);
        spu.setCategoryIds(List.of(101L));
        when(productSpuApi.getSpuList(List.of(10L))).thenReturn(List.of(spu));
        when(productCategoryApi.getSelfAndAncestorCategoryIds(anyCollection())).thenReturn(Set.of(101L, 100L));

        RewardActivityDO activity = new RewardActivityDO();
        activity.setId(1L);
        activity.setName("parent category activity");
        activity.setStatus(CommonStatusEnum.ENABLE.getStatus());
        activity.setStartTime(LocalDateTime.now().minusDays(1));
        activity.setEndTime(LocalDateTime.now().plusDays(1));
        activity.setConditionType(PromotionConditionTypeEnum.PRICE.getType());
        activity.setProductScope(PromotionProductScopeEnum.CATEGORY.getScope());
        activity.setProductScopeValues(List.of(100L));
        activity.setRules(Collections.emptyList());
        when(rewardActivityMapper.selectListBySpuIdAndStatusAndNow(eq(List.of(10L)), eq(Set.of(101L, 100L)),
                eq(CommonStatusEnum.ENABLE.getStatus()))).thenReturn(List.of(activity));

        List<RewardActivityMatchRespDTO> activities = rewardActivityService.getMatchRewardActivityListBySpuIds(List.of(10L));

        assertEquals(1, activities.size());
        assertEquals(List.of(10L), activities.get(0).getSpuIds());
    }

}
