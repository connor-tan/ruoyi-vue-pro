package cn.iocoder.yudao.module.product.controller.admin.publicationtitle.vo;

import lombok.Data;

@Data
public class ProductPublicationTitleSimpleRespVO {

    private Long id;

    private String code;

    private String name;

    private Long typeId;

    private String typeName;

    private String typeCode;

    private String typeIdentifierRule;

    private Long publisherId;

    private String publisherName;

    private String issueCycle;
}
