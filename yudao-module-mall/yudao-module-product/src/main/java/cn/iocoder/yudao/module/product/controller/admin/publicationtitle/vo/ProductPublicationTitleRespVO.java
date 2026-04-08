package cn.iocoder.yudao.module.product.controller.admin.publicationtitle.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductPublicationTitleRespVO {

    private Long id;

    private String code;

    private String name;

    private Long typeId;

    private String typeName;

    private String typeCode;

    private Long publisherId;

    private String publisherName;

    private String issueCycle;

    private Integer status;

    private String issn;

    private String cnCode;

    private String postDistributionCode;

    private String remark;

    private LocalDateTime createTime;
}
