package cn.iocoder.yudao.module.product.controller.admin.publicationspugrade.vo;

import lombok.Data;

import java.util.List;

@Data
public class ProductPublicationSpuGradeRespVO {

    private Long productSpuId;

    private List<Long> gradeCatalogIds;

    private List<String> gradeNames;
}
