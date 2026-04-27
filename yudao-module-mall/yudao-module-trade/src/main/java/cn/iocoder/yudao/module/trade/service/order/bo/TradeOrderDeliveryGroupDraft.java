package cn.iocoder.yudao.module.trade.service.order.bo;

import cn.iocoder.yudao.module.member.api.address.dto.MemberAddressRespDTO;
import cn.iocoder.yudao.module.publication.api.enums.BizSceneEnum;
import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityRespDTO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class TradeOrderDeliveryGroupDraft {

    private final String key;

    private final String bizScene;

    private final Integer deliveryType;

    private MemberAddressRespDTO address;

    private final List<Integer> itemIndexes = new ArrayList<>();

    private List<TradePriceCalculateRespBO.OrderItem> sourceItems;

    private Long previewDeliveryId;

    private TradeOrderDeliveryDO persistedDelivery;

    private Long studentId;

    private String studentNameSnapshot;

    private Long schoolId;

    private String schoolNameSnapshot;

    private Long classId;

    private String classNameSnapshot;

    private Long gradeCatalogId;

    private String gradeNameSnapshot;

    private Long stationId;

    private String stationNameSnapshot;

    private String stationAddressSnapshot;

    private String contactName;

    private String contactMobile;

    public static TradeOrderDeliveryGroupDraft forNormal(Integer deliveryType) {
        return new TradeOrderDeliveryGroupDraft(buildKey(BizSceneEnum.NORMAL.getCode(), null, deliveryType),
                BizSceneEnum.NORMAL.getCode(), deliveryType);
    }

    public static TradeOrderDeliveryGroupDraft forPublication(SubscriptionOrderEligibilityRespDTO student,
                                                              Integer deliveryType) {
        TradeOrderDeliveryGroupDraft group = new TradeOrderDeliveryGroupDraft(buildKey(BizSceneEnum.PUBLICATION.getCode(),
                student.getStudentId(), deliveryType), BizSceneEnum.PUBLICATION.getCode(), deliveryType);
        group.setStudentId(student.getStudentId());
        group.setStudentNameSnapshot(student.getStudentNameSnapshot());
        group.setSchoolId(student.getSchoolId());
        group.setSchoolNameSnapshot(student.getSchoolNameSnapshot());
        group.setClassId(student.getClassId());
        group.setClassNameSnapshot(student.getClassNameSnapshot());
        group.setGradeCatalogId(student.getGradeCatalogId());
        group.setGradeNameSnapshot(student.getGradeNameSnapshot());
        group.setStationId(student.getStationId());
        group.setStationNameSnapshot(student.getStationNameSnapshot());
        group.setStationAddressSnapshot(student.getStationAddressSnapshot());
        group.setContactName(student.getContactName());
        group.setContactMobile(student.getContactMobile());
        return group;
    }

    public static TradeOrderDeliveryGroupDraft forPublicationItem(TradePriceCalculateRespBO.OrderItem item) {
        TradeOrderDeliveryGroupDraft group = new TradeOrderDeliveryGroupDraft(buildKey(BizSceneEnum.PUBLICATION.getCode(),
                item.getSubscriptionStudentId(), item.getResolvedDeliveryType()),
                BizSceneEnum.PUBLICATION.getCode(), item.getResolvedDeliveryType());
        group.setStudentId(item.getSubscriptionStudentId());
        group.setStudentNameSnapshot(item.getSubscriptionStudentNameSnapshot());
        group.setSchoolId(item.getSubscriptionSchoolId());
        group.setSchoolNameSnapshot(item.getSubscriptionSchoolNameSnapshot());
        group.setClassId(item.getSubscriptionClassId());
        group.setClassNameSnapshot(item.getSubscriptionClassNameSnapshot());
        group.setGradeCatalogId(item.getSubscriptionGradeCatalogId());
        group.setGradeNameSnapshot(item.getSubscriptionGradeNameSnapshot());
        group.setStationId(item.getSubscriptionStationId());
        group.setStationNameSnapshot(item.getSubscriptionStationNameSnapshot());
        group.setStationAddressSnapshot(item.getSubscriptionStationAddressSnapshot());
        group.setContactName(item.getSubscriptionContactName());
        group.setContactMobile(item.getSubscriptionContactMobile());
        return group;
    }

    private static String buildKey(String bizScene, Long studentId, Integer deliveryType) {
        return bizScene + ":" + (studentId == null ? "none" : studentId) + ":" + deliveryType;
    }

}
