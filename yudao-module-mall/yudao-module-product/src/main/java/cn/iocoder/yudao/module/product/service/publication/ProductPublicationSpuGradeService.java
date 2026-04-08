package cn.iocoder.yudao.module.product.service.publication;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.GradeCatalogSimpleRespVO;
import cn.iocoder.yudao.module.edu.service.school.SchoolService;
import cn.iocoder.yudao.module.product.controller.admin.publicationspugrade.vo.ProductPublicationGradeSimpleRespVO;
import cn.iocoder.yudao.module.product.controller.admin.publicationspugrade.vo.ProductPublicationSpuGradeRespVO;
import cn.iocoder.yudao.module.product.controller.admin.publicationspugrade.vo.ProductPublicationSpuGradeSaveReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductSpuGradeDO;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductSpuGradeMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class ProductPublicationSpuGradeService {

    @Resource
    private ProductSpuGradeMapper productSpuGradeMapper;
    @Resource
    private SchoolService schoolService;

    public ProductPublicationSpuGradeRespVO getBySpuId(Long productSpuId) {
        List<ProductSpuGradeDO> list = productSpuGradeMapper.selectListByProductSpuId(productSpuId);
        ProductPublicationSpuGradeRespVO respVO = new ProductPublicationSpuGradeRespVO();
        respVO.setProductSpuId(productSpuId);
        List<Long> gradeCatalogIds = CollectionUtils.convertList(list, ProductSpuGradeDO::getGradeCatalogId);
        respVO.setGradeCatalogIds(gradeCatalogIds);
        Map<Long, GradeCatalogSimpleRespVO> gradeMap = CollectionUtils.convertMap(schoolService.getGradeCatalogList(), GradeCatalogSimpleRespVO::getId);
        respVO.setGradeNames(CollectionUtils.convertList(gradeCatalogIds,
                item -> gradeMap.get(item) == null ? null : gradeMap.get(item).getGradeName()));
        return respVO;
    }

    public List<ProductPublicationGradeSimpleRespVO> getSimpleList() {
        return CollectionUtils.convertList(schoolService.getGradeCatalogList(), grade -> {
            ProductPublicationGradeSimpleRespVO respVO = new ProductPublicationGradeSimpleRespVO();
            respVO.setId(grade.getId());
            respVO.setGradeNo(grade.getGradeNo());
            respVO.setGradeName(grade.getGradeName());
            respVO.setAliasName(grade.getAliasName());
            return respVO;
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void createOrUpdate(ProductPublicationSpuGradeSaveReqVO reqVO) {
        replaceGrades(reqVO.getProductSpuId(), reqVO.getGradeCatalogIds());
    }

    @Transactional(rollbackFor = Exception.class)
    public void replaceGrades(Long productSpuId, List<Long> gradeCatalogIds) {
        productSpuGradeMapper.deleteByProductSpuId(productSpuId);
        if (gradeCatalogIds == null || gradeCatalogIds.isEmpty()) {
            return;
        }
        List<ProductSpuGradeDO> relations = CollectionUtils.convertList(gradeCatalogIds,
                gradeCatalogId -> ProductSpuGradeDO.builder().productSpuId(productSpuId).gradeCatalogId(gradeCatalogId).build());
        productSpuGradeMapper.insertBatch(relations);
    }

    public List<Long> getGradeCatalogIds(Long productSpuId) {
        List<ProductSpuGradeDO> relations = productSpuGradeMapper.selectListByProductSpuId(productSpuId);
        if (relations.isEmpty()) {
            return Collections.emptyList();
        }
        return CollectionUtils.convertList(relations, ProductSpuGradeDO::getGradeCatalogId);
    }
}
