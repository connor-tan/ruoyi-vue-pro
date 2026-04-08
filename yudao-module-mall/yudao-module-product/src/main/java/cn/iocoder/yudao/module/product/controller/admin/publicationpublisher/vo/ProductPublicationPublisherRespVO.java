package cn.iocoder.yudao.module.product.controller.admin.publicationpublisher.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductPublicationPublisherRespVO {

    private Long id;

    private String code;

    private String name;

    private Integer sort;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;
}
