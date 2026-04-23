package cn.iocoder.yudao.module.product.dal.mysql.publication;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.product.controller.admin.publicationtype.vo.ProductPublicationTypePageReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationTypeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductPublicationTypeMapper extends BaseMapperX<ProductPublicationTypeDO> {

    default PageResult<ProductPublicationTypeDO> selectPage(ProductPublicationTypePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ProductPublicationTypeDO>()
                .likeIfPresent(ProductPublicationTypeDO::getName, reqVO.getName())
                .eqIfPresent(ProductPublicationTypeDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ProductPublicationTypeDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(ProductPublicationTypeDO::getSort)
                .orderByDesc(ProductPublicationTypeDO::getId));
    }

    default ProductPublicationTypeDO selectByName(String name) {
        return selectOne(ProductPublicationTypeDO::getName, name);
    }

    default List<ProductPublicationTypeDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<ProductPublicationTypeDO>()
                .eqIfPresent(ProductPublicationTypeDO::getStatus, status)
                .orderByAsc(ProductPublicationTypeDO::getSort)
                .orderByDesc(ProductPublicationTypeDO::getId));
    }
}
