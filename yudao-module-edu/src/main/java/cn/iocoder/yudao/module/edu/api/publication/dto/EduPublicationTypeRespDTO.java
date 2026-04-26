package cn.iocoder.yudao.module.edu.api.publication.dto;

import lombok.Data;

@Data
public class EduPublicationTypeRespDTO {

    private Long id;

    private String name;

    private String identifierRule;

    private Integer status;
}
