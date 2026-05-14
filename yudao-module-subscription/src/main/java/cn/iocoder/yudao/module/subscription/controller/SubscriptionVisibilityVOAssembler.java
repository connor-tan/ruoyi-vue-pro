package cn.iocoder.yudao.module.subscription.controller;

import cn.iocoder.yudao.module.edu.api.student.dto.EduStudentSubscriptionContextRespDTO;
import cn.iocoder.yudao.module.product.api.publication.dto.ProductPublicationRespDTO;
import cn.iocoder.yudao.module.subscription.controller.admin.preview.vo.SubscriptionRulePreviewRespVO;
import cn.iocoder.yudao.module.subscription.controller.app.vo.AppSubscriptionPublicationRespVO;
import cn.iocoder.yudao.module.subscription.controller.app.vo.AppSubscriptionWindowRespVO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionRuleDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.service.visibility.SubscriptionVisibilityResultBO;

import java.util.Collections;
import java.util.List;

public final class SubscriptionVisibilityVOAssembler {

    private SubscriptionVisibilityVOAssembler() {
    }

    public static SubscriptionRulePreviewRespVO.Window buildWindow(SubscriptionWindowDO window) {
        if (window == null) {
            return null;
        }
        SubscriptionRulePreviewRespVO.Window vo = new SubscriptionRulePreviewRespVO.Window();
        vo.setId(window.getId());
        vo.setName(window.getName());
        vo.setTargetYearCatalogId(window.getTargetYearCatalogId());
        vo.setTargetYearNameSnapshot(window.getTargetYearNameSnapshot());
        vo.setTargetYearStart(window.getTargetYearStart());
        vo.setTargetYearEnd(window.getTargetYearEnd());
        return vo;
    }

    public static SubscriptionRulePreviewRespVO.Student buildStudent(EduStudentSubscriptionContextRespDTO student) {
        if (student == null) {
            return null;
        }
        SubscriptionRulePreviewRespVO.Student vo = new SubscriptionRulePreviewRespVO.Student();
        vo.setStudentId(student.getStudentId());
        vo.setStudentName(student.getStudentName());
        vo.setSchoolId(student.getSchoolId());
        vo.setSchoolName(student.getSchoolName());
        vo.setClassId(student.getClassId());
        vo.setClassName(student.getClassName());
        vo.setGradeCatalogId(student.getGradeCatalogId());
        vo.setGradeName(student.getGradeName());
        vo.setGradeResolveSource(student.getGradeResolveSource());
        vo.setStationId(student.getStationId());
        vo.setStationName(student.getStationName());
        vo.setBlockedReason(student.getBlockedReason());
        vo.setBlockedReasonDesc(student.getBlockedReasonDesc());
        return vo;
    }

    public static List<SubscriptionRulePreviewRespVO.OfferDecision> buildDecisions(
            List<SubscriptionVisibilityResultBO.OfferDecision> decisions) {
        if (decisions == null) {
            return Collections.emptyList();
        }
        return decisions.stream().map(SubscriptionVisibilityVOAssembler::buildDecision).toList();
    }

    public static List<SubscriptionRulePreviewRespVO.OfferDecision> buildVisibleOffers(
            List<SubscriptionVisibilityResultBO.VisibleOffer> offers) {
        if (offers == null) {
            return Collections.emptyList();
        }
        return offers.stream().map(offer -> {
            SubscriptionRulePreviewRespVO.OfferDecision vo = buildOfferBase(offer.getOffer(), offer.getPublication());
            vo.setVisible(true);
            vo.setReason(offer.getReason());
            vo.setReasonDesc(offer.getReasonDesc());
            fillMatchedRule(vo, offer.getMatchedRule());
            vo.setGradeApplicabilityOverride(offer.getGradeApplicabilityOverride());
            vo.setFinalSkus(buildSkus(offer.getSkus()));
            vo.setFinalSkuCount(vo.getFinalSkus().size());
            vo.setCandidateSkuCount(vo.getFinalSkuCount());
            vo.setTotalOfferSkuCount(vo.getFinalSkuCount());
            return vo;
        }).toList();
    }

    public static AppSubscriptionWindowRespVO.Window buildAppWindow(SubscriptionWindowDO window) {
        if (window == null) {
            return null;
        }
        AppSubscriptionWindowRespVO.Window vo = new AppSubscriptionWindowRespVO.Window();
        vo.setId(window.getId());
        vo.setName(window.getName());
        vo.setTargetYearCatalogId(window.getTargetYearCatalogId());
        vo.setTargetYearNameSnapshot(window.getTargetYearNameSnapshot());
        vo.setTargetYearStart(window.getTargetYearStart());
        vo.setTargetYearEnd(window.getTargetYearEnd());
        return vo;
    }

    public static AppSubscriptionWindowRespVO.Student buildAppStudent(EduStudentSubscriptionContextRespDTO student) {
        if (student == null) {
            return null;
        }
        AppSubscriptionWindowRespVO.Student vo = new AppSubscriptionWindowRespVO.Student();
        vo.setStudentId(student.getStudentId());
        vo.setStudentName(student.getStudentName());
        vo.setSchoolId(student.getSchoolId());
        vo.setSchoolName(student.getSchoolName());
        vo.setClassId(student.getClassId());
        vo.setClassName(student.getClassName());
        vo.setGradeCatalogId(student.getGradeCatalogId());
        vo.setGradeName(student.getGradeName());
        vo.setGradeResolveSource(student.getGradeResolveSource());
        vo.setStationId(student.getStationId());
        vo.setStationName(student.getStationName());
        vo.setBlockedReason(student.getBlockedReason());
        vo.setBlockedReasonDesc(student.getBlockedReasonDesc());
        return vo;
    }

    public static List<AppSubscriptionPublicationRespVO.Offer> buildAppVisibleOffers(
            List<SubscriptionVisibilityResultBO.VisibleOffer> offers) {
        if (offers == null) {
            return Collections.emptyList();
        }
        return offers.stream().map(offer -> {
            AppSubscriptionPublicationRespVO.Offer vo = buildAppOfferBase(offer.getOffer(), offer.getPublication());
            vo.setVisible(true);
            vo.setReason(offer.getReason());
            vo.setReasonDesc(offer.getReasonDesc());
            fillAppMatchedRule(vo, offer.getMatchedRule());
            vo.setGradeApplicabilityOverride(offer.getGradeApplicabilityOverride());
            vo.setFinalSkus(buildAppSkus(offer.getSkus()));
            vo.setFinalSkuCount(vo.getFinalSkus().size());
            vo.setCandidateSkuCount(vo.getFinalSkuCount());
            vo.setTotalOfferSkuCount(vo.getFinalSkuCount());
            return vo;
        }).toList();
    }

    private static SubscriptionRulePreviewRespVO.OfferDecision buildDecision(
            SubscriptionVisibilityResultBO.OfferDecision decision) {
        SubscriptionRulePreviewRespVO.OfferDecision vo = buildOfferBase(decision.getOffer(), decision.getPublication());
        vo.setVisible(decision.getVisible());
        vo.setReason(decision.getReason());
        vo.setReasonDesc(decision.getReasonDesc());
        vo.setTotalOfferSkuCount(decision.getTotalOfferSkuCount());
        vo.setCandidateSkuCount(decision.getCandidateSkuCount());
        vo.setFinalSkuCount(decision.getFinalSkuCount());
        fillMatchedRule(vo, decision.getMatchedRule());
        vo.setGradeApplicabilityOverride(decision.getGradeApplicabilityOverride());
        vo.setFinalSkus(buildSkus(decision.getFinalSkus()));
        vo.setDiagnosticSkus(buildSkus(decision.getDiagnosticSkus()));
        return vo;
    }

    private static SubscriptionRulePreviewRespVO.OfferDecision buildOfferBase(
            cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferDO offer,
            ProductPublicationRespDTO publication) {
        SubscriptionRulePreviewRespVO.OfferDecision vo = new SubscriptionRulePreviewRespVO.OfferDecision();
        if (offer != null) {
            vo.setOfferId(offer.getId());
            vo.setProductSpuId(offer.getProductSpuId());
        }
        if (publication != null) {
            vo.setProductName(publication.getName());
            vo.setPicUrl(publication.getPicUrl());
        }
        return vo;
    }

    private static List<SubscriptionRulePreviewRespVO.OfferSku> buildSkus(
            List<SubscriptionVisibilityResultBO.VisibleOfferSku> skus) {
        if (skus == null) {
            return Collections.emptyList();
        }
        return skus.stream().map(item -> {
            SubscriptionRulePreviewRespVO.OfferSku vo = new SubscriptionRulePreviewRespVO.OfferSku();
            if (item.getOfferSku() != null) {
                vo.setOfferSkuId(item.getOfferSku().getId());
                vo.setProductSkuId(item.getOfferSku().getProductSkuId());
            }
            ProductPublicationRespDTO.PublicationSkuDTO productSku = item.getProductSku();
            if (productSku != null) {
                vo.setProductSkuId(productSku.getId());
                vo.setProductSkuName(productSku.getName());
                vo.setPrice(productSku.getPrice());
                vo.setStock(productSku.getStock());
                vo.setApplicableGradeCatalogIds(productSku.getApplicableGradeCatalogIds());
                vo.setApplicableGradeNames(productSku.getApplicableGradeNames());
                ProductPublicationRespDTO.PublicationSkuExtDTO ext = productSku.getPublicationExt();
                if (ext != null) {
                    vo.setVolumeLabel(ext.getVolumeLabel());
                    vo.setEditionLabel(ext.getEditionLabel());
                    vo.setIsbn(ext.getIsbn());
                }
            }
            vo.setDecisionStatus(item.getDecisionStatus());
            vo.setDecisionStatusName(item.getDecisionStatusName());
            vo.setReason(item.getReason());
            vo.setGradeApplicabilityOverride(item.getGradeApplicabilityOverride());
            if (item.getMatchedRule() != null) {
                vo.setMatchedRuleId(item.getMatchedRule().getId());
                vo.setMatchedRuleName(item.getMatchedRule().getName());
            }
            return vo;
        }).toList();
    }

    private static AppSubscriptionPublicationRespVO.Offer buildAppOfferBase(
            cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferDO offer,
            ProductPublicationRespDTO publication) {
        AppSubscriptionPublicationRespVO.Offer vo = new AppSubscriptionPublicationRespVO.Offer();
        if (offer != null) {
            vo.setOfferId(offer.getId());
            vo.setProductSpuId(offer.getProductSpuId());
        }
        if (publication != null) {
            vo.setProductName(publication.getName());
            vo.setPicUrl(publication.getPicUrl());
        }
        return vo;
    }

    private static List<AppSubscriptionPublicationRespVO.OfferSku> buildAppSkus(
            List<SubscriptionVisibilityResultBO.VisibleOfferSku> skus) {
        if (skus == null) {
            return Collections.emptyList();
        }
        return skus.stream().map(item -> {
            AppSubscriptionPublicationRespVO.OfferSku vo = new AppSubscriptionPublicationRespVO.OfferSku();
            if (item.getOfferSku() != null) {
                vo.setOfferSkuId(item.getOfferSku().getId());
                vo.setProductSkuId(item.getOfferSku().getProductSkuId());
                vo.setMaxQuantityPerStudent(item.getOfferSku().getMaxQuantityPerStudent());
            }
            ProductPublicationRespDTO.PublicationSkuDTO productSku = item.getProductSku();
            if (productSku != null) {
                vo.setProductSkuId(productSku.getId());
                vo.setProductSkuName(productSku.getName());
                vo.setPrice(productSku.getPrice());
                vo.setStock(productSku.getStock());
                vo.setApplicableGradeCatalogIds(productSku.getApplicableGradeCatalogIds());
                vo.setApplicableGradeNames(productSku.getApplicableGradeNames());
                ProductPublicationRespDTO.PublicationSkuExtDTO ext = productSku.getPublicationExt();
                if (ext != null) {
                    vo.setVolumeLabel(ext.getVolumeLabel());
                    vo.setEditionLabel(ext.getEditionLabel());
                    vo.setIsbn(ext.getIsbn());
                }
            }
            vo.setReason(item.getReason());
            vo.setGradeApplicabilityOverride(item.getGradeApplicabilityOverride());
            if (item.getMatchedRule() != null) {
                vo.setMatchedRuleId(item.getMatchedRule().getId());
                vo.setMatchedRuleName(item.getMatchedRule().getName());
            }
            return vo;
        }).toList();
    }

    private static void fillMatchedRule(SubscriptionRulePreviewRespVO.OfferDecision vo, SubscriptionRuleDO rule) {
        if (rule == null) {
            return;
        }
        vo.setMatchedRuleId(rule.getId());
        vo.setMatchedRuleName(rule.getName());
    }

    private static void fillAppMatchedRule(AppSubscriptionPublicationRespVO.Offer vo, SubscriptionRuleDO rule) {
        if (rule == null) {
            return;
        }
        vo.setMatchedRuleId(rule.getId());
        vo.setMatchedRuleName(rule.getName());
    }

}
