package cn.iocoder.yudao.module.product.dal.mysql.publication;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationSkuIssueTemplateDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ProductPublicationSkuIssueTemplateMapper
        extends BaseMapperX<ProductPublicationSkuIssueTemplateDO> {

    default List<ProductPublicationSkuIssueTemplateDO> selectListBySkuId(Long skuId) {
        return selectList(new LambdaQueryWrapperX<ProductPublicationSkuIssueTemplateDO>()
                .eq(ProductPublicationSkuIssueTemplateDO::getSkuId, skuId)
                .orderByAsc(ProductPublicationSkuIssueTemplateDO::getSort)
                .orderByAsc(ProductPublicationSkuIssueTemplateDO::getIssueNo)
                .orderByAsc(ProductPublicationSkuIssueTemplateDO::getId));
    }

    default List<ProductPublicationSkuIssueTemplateDO> selectListBySkuIds(Collection<Long> skuIds) {
        if (CollUtil.isEmpty(skuIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ProductPublicationSkuIssueTemplateDO>()
                .in(ProductPublicationSkuIssueTemplateDO::getSkuId, skuIds)
                .orderByAsc(ProductPublicationSkuIssueTemplateDO::getSkuId)
                .orderByAsc(ProductPublicationSkuIssueTemplateDO::getSort)
                .orderByAsc(ProductPublicationSkuIssueTemplateDO::getIssueNo)
                .orderByAsc(ProductPublicationSkuIssueTemplateDO::getId));
    }

    default List<ProductPublicationSkuIssueTemplateDO> selectEnabledListBySkuId(Long skuId, Integer status) {
        return selectList(new LambdaQueryWrapperX<ProductPublicationSkuIssueTemplateDO>()
                .eq(ProductPublicationSkuIssueTemplateDO::getSkuId, skuId)
                .eq(ProductPublicationSkuIssueTemplateDO::getStatus, status)
                .orderByAsc(ProductPublicationSkuIssueTemplateDO::getSort)
                .orderByAsc(ProductPublicationSkuIssueTemplateDO::getIssueNo)
                .orderByAsc(ProductPublicationSkuIssueTemplateDO::getId));
    }

    default ProductPublicationSkuIssueTemplateDO selectBySkuIdAndIssueNoAndIdNot(Long skuId, Integer issueNo,
                                                                                 Long excludeId) {
        return selectOne(new LambdaQueryWrapperX<ProductPublicationSkuIssueTemplateDO>()
                .eq(ProductPublicationSkuIssueTemplateDO::getSkuId, skuId)
                .eq(ProductPublicationSkuIssueTemplateDO::getIssueNo, issueNo)
                .ne(excludeId != null, ProductPublicationSkuIssueTemplateDO::getId, excludeId));
    }

    default int deleteBySkuIds(Collection<Long> skuIds) {
        if (CollUtil.isEmpty(skuIds)) {
            return 0;
        }
        return delete(new LambdaQueryWrapperX<ProductPublicationSkuIssueTemplateDO>()
                .in(ProductPublicationSkuIssueTemplateDO::getSkuId, skuIds));
    }

}
