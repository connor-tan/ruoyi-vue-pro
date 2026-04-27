package cn.iocoder.yudao.module.subscription.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.subscription.controller.SubscriptionVisibilityVOAssembler;
import cn.iocoder.yudao.module.subscription.controller.app.vo.AppSubscriptionPublicationRespVO;
import cn.iocoder.yudao.module.subscription.service.visibility.SubscriptionVisibilityResultBO;
import cn.iocoder.yudao.module.subscription.service.visibility.SubscriptionVisibilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 订刊刊物")
@RestController
@RequestMapping("/subscription/app/publication")
@Validated
public class AppSubscriptionPublicationController {

    @Resource
    private SubscriptionVisibilityService visibilityService;

    @GetMapping("/list")
    @Operation(summary = "获得学生可订刊物列表")
    @Parameter(name = "studentId", required = true)
    public CommonResult<AppSubscriptionPublicationRespVO> list(@RequestParam("studentId") Long studentId,
                                                               @RequestParam(value = "productSpuIds", required = false)
                                                               Set<Long> productSpuIds) {
        SubscriptionVisibilityResultBO result = visibilityService.calculate(getLoginUserId(), studentId, null);
        List<SubscriptionVisibilityResultBO.VisibleOffer> visibleOffers = result.getVisibleOffers();
        if (productSpuIds != null && !productSpuIds.isEmpty()) {
            visibleOffers = visibleOffers.stream()
                    .filter(offer -> offer.getOffer() != null && productSpuIds.contains(offer.getOffer().getProductSpuId()))
                    .toList();
        }
        AppSubscriptionPublicationRespVO respVO = new AppSubscriptionPublicationRespVO();
        respVO.setWindow(SubscriptionVisibilityVOAssembler.buildAppWindow(result.getWindow()));
        respVO.setStudent(SubscriptionVisibilityVOAssembler.buildAppStudent(result.getStudent()));
        respVO.setBlockedReason(result.getBlockedReason());
        respVO.setBlockedReasonDesc(result.getBlockedReasonDesc());
        respVO.setOffers(SubscriptionVisibilityVOAssembler.buildAppVisibleOffers(visibleOffers));
        return success(respVO);
    }

}
