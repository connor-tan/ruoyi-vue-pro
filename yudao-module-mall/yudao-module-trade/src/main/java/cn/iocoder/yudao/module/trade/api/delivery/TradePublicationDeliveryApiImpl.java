package cn.iocoder.yudao.module.trade.api.delivery;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.module.trade.api.delivery.dto.TradePublicationDeliveryCandidateGroupRespDTO;
import cn.iocoder.yudao.module.trade.api.delivery.dto.TradePublicationDeliveryCandidateItemRespDTO;
import cn.iocoder.yudao.module.trade.api.delivery.dto.TradePublicationDeliveryCandidatePageReqDTO;
import cn.iocoder.yudao.module.trade.api.delivery.dto.TradePublicationDeliveryCandidateRespDTO;
import cn.iocoder.yudao.module.trade.api.delivery.dto.TradePublicationDeliveryConfirmReqDTO;
import cn.iocoder.yudao.module.trade.api.delivery.dto.TradePublicationDeliveryCreateReqDTO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderPublicationIssueMapper;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.enums.delivery.PublicationDeliveryStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.service.delivery.DeliveryExpressService;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderPublicationIssueService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PUBLICATION_DELIVERY_ITEM_UPDATE_FAIL;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PUBLICATION_EXPRESS_BATCH_TOO_LARGE;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PUBLICATION_EXPRESS_LOGISTICS_REQUIRED;

/**
 * 刊物发货 API 实现。
 *
 * <p>只负责订单期次可发判断和状态回写，不创建仓库出库批次。</p>
 */
@Service
@Validated
public class TradePublicationDeliveryApiImpl implements TradePublicationDeliveryApi {

    private static final int EXPRESS_BATCH_ITEM_LIMIT = 500;

    @Resource
    private TradeOrderPublicationIssueMapper publicationIssueMapper;
    @Resource
    private DeliveryExpressService deliveryExpressService;
    @Resource
    private TradeOrderPublicationIssueService publicationIssueService;

    @Override
    public PageResult<TradePublicationDeliveryCandidateRespDTO> getCandidatePage(
            TradePublicationDeliveryCandidatePageReqDTO reqDTO) {
        IPage<TradePublicationDeliveryCandidateRespDTO> page = publicationIssueMapper
                .selectPublicationDeliveryCandidatePage(MyBatisUtils.buildPage(reqDTO), reqDTO,
                        TradeOrderStatusEnum.UNDELIVERED.getStatus(),
                        PublicationDeliveryStatusEnum.UNDELIVERED.getStatus());
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public PageResult<TradePublicationDeliveryCandidateGroupRespDTO> getCandidateGroupPage(
            TradePublicationDeliveryCandidatePageReqDTO reqDTO) {
        IPage<TradePublicationDeliveryCandidateGroupRespDTO> page = publicationIssueMapper
                .selectPublicationDeliveryCandidateGroupPage(MyBatisUtils.buildPage(reqDTO), reqDTO,
                        TradeOrderStatusEnum.UNDELIVERED.getStatus(),
                        PublicationDeliveryStatusEnum.UNDELIVERED.getStatus());
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public List<TradePublicationDeliveryCandidateRespDTO> getCandidateList(
            TradePublicationDeliveryCandidatePageReqDTO reqDTO) {
        return publicationIssueMapper.selectPublicationDeliveryCandidateChildList(reqDTO,
                TradeOrderStatusEnum.UNDELIVERED.getStatus(), PublicationDeliveryStatusEnum.UNDELIVERED.getStatus());
    }

    @Override
    public List<TradePublicationDeliveryCandidateRespDTO> getCandidateChildList(
            TradePublicationDeliveryCandidatePageReqDTO reqDTO) {
        validateGroupScopeReq(reqDTO);
        return publicationIssueMapper.selectPublicationDeliveryCandidateChildList(reqDTO,
                TradeOrderStatusEnum.UNDELIVERED.getStatus(), PublicationDeliveryStatusEnum.UNDELIVERED.getStatus());
    }

    @Override
    public PageResult<TradePublicationDeliveryCandidateRespDTO> getCandidateChildPage(
            TradePublicationDeliveryCandidatePageReqDTO reqDTO) {
        validateGroupScopeReq(reqDTO);
        IPage<TradePublicationDeliveryCandidateRespDTO> page = publicationIssueMapper
                .selectPublicationDeliveryCandidateChildPage(MyBatisUtils.buildPage(reqDTO), reqDTO,
                        TradeOrderStatusEnum.UNDELIVERED.getStatus(),
                        PublicationDeliveryStatusEnum.UNDELIVERED.getStatus());
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public List<TradePublicationDeliveryCandidateItemRespDTO> getCandidateItemList(
            TradePublicationDeliveryCandidatePageReqDTO reqDTO) {
        List<TradePublicationDeliveryCandidateItemRespDTO> items = selectCandidateItemList(reqDTO);
        validateExpressBatchSize(reqDTO.getDeliveryType(), items);
        return items;
    }

    @Override
    public List<TradePublicationDeliveryCandidateItemRespDTO> getDeliverableItemList(
            TradePublicationDeliveryCreateReqDTO reqDTO) {
        validateCreateReq(reqDTO);
        TradePublicationDeliveryCandidatePageReqDTO candidateReqDTO = buildCandidateReqDTO(reqDTO);
        List<TradePublicationDeliveryCandidateItemRespDTO> items = selectCandidateItemList(candidateReqDTO);
        if (CollUtil.isEmpty(items)) {
            throw exception(PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND);
        }
        validateExpressBatchSize(reqDTO.getDeliveryType(), items);
        buildExpressItemMap(reqDTO, items);
        return items;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmDelivered(TradePublicationDeliveryConfirmReqDTO reqDTO) {
        if (reqDTO.getDeliveryBatchId() == null || reqDTO.getDeliveryTime() == null || CollUtil.isEmpty(reqDTO.getItems())) {
            throw exception(PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND);
        }
        if (Objects.equals(reqDTO.getDeliveryType(), DeliveryTypeEnum.SCHOOL.getType())) {
            Set<Long> orderIssueIds = convertSet(reqDTO.getItems(), TradePublicationDeliveryConfirmReqDTO.Item::getOrderIssueId);
            int updatedCount = publicationIssueMapper.updateDeliveredByIds(orderIssueIds,
                    PublicationDeliveryStatusEnum.UNDELIVERED.getStatus(), reqDTO.getDeliveryBatchId(),
                    reqDTO.getDeliveryTime());
            if (updatedCount != reqDTO.getItems().size()) {
                throw exception(PUBLICATION_DELIVERY_ITEM_UPDATE_FAIL);
            }
            publicationIssueService.afterIssueDelivered(orderIssueIds, reqDTO.getDeliveryTime());
            return;
        }
        if (!Objects.equals(reqDTO.getDeliveryType(), DeliveryTypeEnum.EXPRESS.getType())) {
            throw exception(PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND);
        }
        int updatedCount = 0;
        for (TradePublicationDeliveryConfirmReqDTO.Item item : reqDTO.getItems()) {
            if (item.getOrderIssueId() == null || item.getLogisticsId() == null || StrUtil.isBlank(item.getLogisticsNo())) {
                throw exception(PUBLICATION_EXPRESS_LOGISTICS_REQUIRED);
            }
            updatedCount += publicationIssueMapper.updateDeliveredById(item.getOrderIssueId(),
                    PublicationDeliveryStatusEnum.UNDELIVERED.getStatus(), reqDTO.getDeliveryBatchId(),
                    reqDTO.getDeliveryTime(), item.getLogisticsId(), item.getLogisticsNo());
        }
        if (updatedCount != reqDTO.getItems().size()) {
            throw exception(PUBLICATION_DELIVERY_ITEM_UPDATE_FAIL);
        }
        publicationIssueService.afterIssueDelivered(
                convertSet(reqDTO.getItems(), TradePublicationDeliveryConfirmReqDTO.Item::getOrderIssueId),
                reqDTO.getDeliveryTime());
    }

    private List<TradePublicationDeliveryCandidateItemRespDTO> selectCandidateItemList(
            TradePublicationDeliveryCandidatePageReqDTO reqDTO) {
        return publicationIssueMapper.selectPublicationDeliveryCandidateItemList(reqDTO,
                TradeOrderStatusEnum.UNDELIVERED.getStatus(), PublicationDeliveryStatusEnum.UNDELIVERED.getStatus(),
                buildCandidateItemLimit(reqDTO.getDeliveryType()));
    }

    private TradePublicationDeliveryCandidatePageReqDTO buildCandidateReqDTO(
            TradePublicationDeliveryCreateReqDTO reqDTO) {
        return new TradePublicationDeliveryCandidatePageReqDTO()
                .setDeliveryType(reqDTO.getDeliveryType())
                .setSchoolId(reqDTO.getSchoolId())
                .setStationId(reqDTO.getStationId())
                .setWarehouseId(reqDTO.getWarehouseId())
                .setWindowId(reqDTO.getWindowId())
                .setOfferId(reqDTO.getOfferId())
                .setOfferSkuId(reqDTO.getOfferSkuId())
                .setSkuId(reqDTO.getSkuId())
                .setIssueId(reqDTO.getIssueId())
                .setIssueNo(reqDTO.getIssueNo());
    }

    private void validateGroupScopeReq(TradePublicationDeliveryCandidatePageReqDTO reqDTO) {
        if (reqDTO.getDeliveryType() == null || reqDTO.getSchoolId() == null || reqDTO.getStationId() == null
                || reqDTO.getWindowId() == null) {
            throw exception(PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND);
        }
        if (reqDTO.getWarehouseId() == null) {
            throw exception(PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND);
        }
        if (Objects.equals(reqDTO.getDeliveryType(), DeliveryTypeEnum.SCHOOL.getType())) {
            return;
        }
        if (!Objects.equals(reqDTO.getDeliveryType(), DeliveryTypeEnum.EXPRESS.getType())) {
            throw exception(PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND);
        }
    }

    private void validateCreateReq(TradePublicationDeliveryCreateReqDTO reqDTO) {
        if (Objects.equals(reqDTO.getDeliveryType(), DeliveryTypeEnum.SCHOOL.getType())) {
            if (reqDTO.getStationId() == null || reqDTO.getWarehouseId() == null) {
                throw exception(PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND);
            }
            return;
        }
        if (Objects.equals(reqDTO.getDeliveryType(), DeliveryTypeEnum.EXPRESS.getType())) {
            if (reqDTO.getStationId() == null || reqDTO.getWarehouseId() == null) {
                throw exception(PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND);
            }
            if (CollUtil.isEmpty(reqDTO.getExpressItems())) {
                throw exception(PUBLICATION_EXPRESS_LOGISTICS_REQUIRED);
            }
            for (TradePublicationDeliveryCreateReqDTO.ExpressItem item : reqDTO.getExpressItems()) {
                if (item.getOrderIssueId() == null || item.getLogisticsId() == null || StrUtil.isBlank(item.getLogisticsNo())) {
                    throw exception(PUBLICATION_EXPRESS_LOGISTICS_REQUIRED);
                }
                deliveryExpressService.validateDeliveryExpress(item.getLogisticsId());
            }
            return;
        }
        throw exception(PUBLICATION_DELIVERY_CANDIDATE_NOT_FOUND);
    }

    private Integer buildCandidateItemLimit(Integer deliveryType) {
        return Objects.equals(deliveryType, DeliveryTypeEnum.EXPRESS.getType()) ? EXPRESS_BATCH_ITEM_LIMIT + 1 : null;
    }

    private void validateExpressBatchSize(Integer deliveryType, List<TradePublicationDeliveryCandidateItemRespDTO> items) {
        if (Objects.equals(deliveryType, DeliveryTypeEnum.EXPRESS.getType()) && items.size() > EXPRESS_BATCH_ITEM_LIMIT) {
            throw exception(PUBLICATION_EXPRESS_BATCH_TOO_LARGE, EXPRESS_BATCH_ITEM_LIMIT);
        }
    }

    private Map<Long, TradePublicationDeliveryCreateReqDTO.ExpressItem> buildExpressItemMap(
            TradePublicationDeliveryCreateReqDTO reqDTO, List<TradePublicationDeliveryCandidateItemRespDTO> items) {
        if (!Objects.equals(reqDTO.getDeliveryType(), DeliveryTypeEnum.EXPRESS.getType())) {
            return null;
        }
        Map<Long, TradePublicationDeliveryCreateReqDTO.ExpressItem> expressItemMap = convertMap(
                reqDTO.getExpressItems(), TradePublicationDeliveryCreateReqDTO.ExpressItem::getOrderIssueId,
                Function.identity());
        Set<Long> orderIssueIds = convertSet(items, TradePublicationDeliveryCandidateItemRespDTO::getOrderIssueId);
        if (!expressItemMap.keySet().containsAll(orderIssueIds) || expressItemMap.size() != orderIssueIds.size()) {
            throw exception(PUBLICATION_EXPRESS_LOGISTICS_REQUIRED);
        }
        return expressItemMap;
    }

}
