package cn.iocoder.yudao.module.subscription.service.offerskuissue;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.product.api.publication.dto.ProductPublicationRespDTO;
import cn.iocoder.yudao.module.publication.api.enums.PublicationIssueModeEnum;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionOfferSkuIssueDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowDO;
import cn.iocoder.yudao.module.subscription.dal.dataobject.SubscriptionWindowOfferSkuDO;
import cn.iocoder.yudao.module.subscription.dal.mysql.SubscriptionOfferSkuIssueMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionOfferSkuIssueDefaultTemplateServiceTest {

    @Mock
    private SubscriptionOfferSkuIssueMapper offerSkuIssueMapper;
    @InjectMocks
    private SubscriptionOfferSkuIssueDefaultTemplateService defaultTemplateService;

    @Test
    void copyDefaultIssuesForNewOfferSkus_shouldCopyEnabledTemplatesWithWindowStartOffsets() {
        SubscriptionWindowDO window = SubscriptionWindowDO.builder()
                .id(1L)
                .startTime(LocalDateTime.of(2026, 9, 1, 0, 0))
                .build();
        SubscriptionWindowOfferSkuDO offerSku = new SubscriptionWindowOfferSkuDO()
                .setId(20L)
                .setOfferId(10L)
                .setProductSkuId(30L);
        ProductPublicationRespDTO publication = publication();
        when(offerSkuIssueMapper.selectListByOfferSkuId(20L)).thenReturn(List.of());

        defaultTemplateService.copyDefaultIssuesForNewOfferSkus(window, publication, List.of(offerSku));

        ArgumentCaptor<List<SubscriptionOfferSkuIssueDO>> captor = ArgumentCaptor.forClass(List.class);
        verify(offerSkuIssueMapper).insertBatch(captor.capture());
        assertEquals(1, captor.getValue().size());
        SubscriptionOfferSkuIssueDO issue = captor.getValue().get(0);
        assertEquals(10L, issue.getOfferId());
        assertEquals(20L, issue.getOfferSkuId());
        assertEquals(1, issue.getIssueNo());
        assertEquals("第1期", issue.getIssueName());
        assertEquals(LocalDate.of(2026, 9, 1), issue.getPlannedPublishDate());
        assertEquals(LocalDate.of(2026, 9, 8), issue.getPlannedDeliveryDate());
    }

    private ProductPublicationRespDTO publication() {
        ProductPublicationRespDTO publication = new ProductPublicationRespDTO();
        ProductPublicationRespDTO.PublicationSpuExtDTO spuExt = new ProductPublicationRespDTO.PublicationSpuExtDTO();
        spuExt.setIssueMode(PublicationIssueModeEnum.PERIODICAL.getCode());
        publication.setPublicationExt(spuExt);
        ProductPublicationRespDTO.PublicationSkuDTO sku = new ProductPublicationRespDTO.PublicationSkuDTO();
        sku.setId(30L);
        ProductPublicationRespDTO.PublicationSkuIssueTemplateDTO enabled =
                new ProductPublicationRespDTO.PublicationSkuIssueTemplateDTO();
        enabled.setIssueNo(1);
        enabled.setIssueName("第1期");
        enabled.setPublishOffsetDays(0);
        enabled.setDeliveryOffsetDays(7);
        enabled.setSort(1);
        enabled.setStatus(CommonStatusEnum.ENABLE.getStatus());
        ProductPublicationRespDTO.PublicationSkuIssueTemplateDTO disabled =
                new ProductPublicationRespDTO.PublicationSkuIssueTemplateDTO();
        disabled.setIssueNo(2);
        disabled.setIssueName("第2期");
        disabled.setStatus(CommonStatusEnum.DISABLE.getStatus());
        sku.setIssueTemplates(List.of(enabled, disabled));
        publication.setSkus(List.of(sku));
        return publication;
    }

}
