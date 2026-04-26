package cn.iocoder.yudao.module.subscription.controller.admin.offer.vo;

import lombok.Data;

import java.util.List;

@Data
public class SubscriptionOfferSaveReqVO {

    private Long id;

    private Long windowId;

    private Long productSpuId;

    private Boolean recommendFlag;

    private Integer sort;

    private Integer status;

    private String remark;

    private List<Long> gradeCatalogIds;

}
