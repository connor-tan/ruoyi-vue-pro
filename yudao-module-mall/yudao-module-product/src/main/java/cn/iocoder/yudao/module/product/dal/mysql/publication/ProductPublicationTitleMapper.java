package cn.iocoder.yudao.module.product.dal.mysql.publication;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.product.controller.admin.publicationtitle.vo.ProductPublicationTitlePageReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationTitleDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ProductPublicationTitleMapper extends BaseMapperX<ProductPublicationTitleDO> {

    default PageResult<ProductPublicationTitleDO> selectPage(ProductPublicationTitlePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ProductPublicationTitleDO>()
                .likeIfPresent(ProductPublicationTitleDO::getName, reqVO.getName())
                .eqIfPresent(ProductPublicationTitleDO::getTypeId, reqVO.getTypeId())
                .eqIfPresent(ProductPublicationTitleDO::getPublisherId, reqVO.getPublisherId())
                .eqIfPresent(ProductPublicationTitleDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ProductPublicationTitleDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ProductPublicationTitleDO::getId));
    }

    default ProductPublicationTitleDO selectByCode(String code) {
        return selectOne(ProductPublicationTitleDO::getCode, code);
    }

    default ProductPublicationTitleDO selectByName(String name) {
        return selectOne(ProductPublicationTitleDO::getName, name);
    }

    default List<ProductPublicationTitleDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<ProductPublicationTitleDO>()
                .eqIfPresent(ProductPublicationTitleDO::getStatus, status)
                .orderByDesc(ProductPublicationTitleDO::getId));
    }

    default List<ProductPublicationTitleDO> selectListByTypeIds(Collection<Long> typeIds) {
        return selectList(new LambdaQueryWrapperX<ProductPublicationTitleDO>()
                .inIfPresent(ProductPublicationTitleDO::getTypeId, typeIds));
    }

    default List<ProductPublicationTitleDO> selectListByPublisherIds(Collection<Long> publisherIds) {
        return selectList(new LambdaQueryWrapperX<ProductPublicationTitleDO>()
                .inIfPresent(ProductPublicationTitleDO::getPublisherId, publisherIds));
    }

    default Long countByPublisherId(Long publisherId) {
        return selectCount(ProductPublicationTitleDO::getPublisherId, publisherId);
    }
}
