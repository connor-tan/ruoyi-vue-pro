package cn.iocoder.yudao.module.edu.api.yearcatalog.dto;

import lombok.Data;

@Data
public class EduYearCatalogRespDTO {

    private Long id;

    private Integer yearStart;

    private Integer yearEnd;

    private String name;

}
