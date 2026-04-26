package cn.iocoder.yudao.module.subscription.service.visibility;

import cn.iocoder.yudao.module.edu.api.student.dto.EduStudentSubscriptionContextRespDTO;
import cn.iocoder.yudao.module.product.api.publication.dto.ProductPublicationRespDTO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionRuleDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferSkuDO;
import lombok.Data;

import java.util.List;

@Data
public class SubscriptionVisibilityResultBO {

    private SubscriptionWindowDO window;

    private EduStudentSubscriptionContextRespDTO student;

    private String blockedReason;

    private String blockedReasonDesc;

    private List<OfferDecision> decisions;

    private List<VisibleOffer> visibleOffers;

    @Data
    public static class OfferDecision {
        private SubscriptionWindowOfferDO offer;
        private ProductPublicationRespDTO publication;
        private Boolean visible;
        private String reason;
        private String reasonDesc;
        private Integer totalOfferSkuCount;
        private Integer candidateSkuCount;
        private Integer finalSkuCount;
        private SubscriptionRuleDO matchedRule;
        private Boolean gradeApplicabilityOverride;
        private List<VisibleOfferSku> finalSkus;
        private List<VisibleOfferSku> diagnosticSkus;
    }

    @Data
    public static class VisibleOffer {
        private SubscriptionWindowOfferDO offer;
        private ProductPublicationRespDTO publication;
        private String reason;
        private String reasonDesc;
        private SubscriptionRuleDO matchedRule;
        private Boolean gradeApplicabilityOverride;
        private List<VisibleOfferSku> skus;
    }

    @Data
    public static class VisibleOfferSku {
        private SubscriptionWindowOfferSkuDO offerSku;
        private ProductPublicationRespDTO.PublicationSkuDTO productSku;
        private String decisionStatus;
        private String decisionStatusName;
        private String reason;
        private SubscriptionRuleDO matchedRule;
        private Boolean gradeApplicabilityOverride;
    }
}
