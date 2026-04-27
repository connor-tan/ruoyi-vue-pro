package cn.iocoder.yudao.module.edu.dal.mysql.publication;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.controller.admin.publicationpublisher.vo.ProductPublicationPublisherPageReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.publication.ProductPublicationPublisherDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductPublicationPublisherMapper extends BaseMapperX<ProductPublicationPublisherDO> {

    default PageResult<ProductPublicationPublisherDO> selectPage(ProductPublicationPublisherPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ProductPublicationPublisherDO>()
                .likeIfPresent(ProductPublicationPublisherDO::getName, reqVO.getName())
                .eqIfPresent(ProductPublicationPublisherDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ProductPublicationPublisherDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(ProductPublicationPublisherDO::getSort)
                .orderByDesc(ProductPublicationPublisherDO::getId));
    }

    default ProductPublicationPublisherDO selectByName(String name) {
        return selectOne(ProductPublicationPublisherDO::getName, name);
    }

    default List<ProductPublicationPublisherDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<ProductPublicationPublisherDO>()
                .eqIfPresent(ProductPublicationPublisherDO::getStatus, status)
                .orderByAsc(ProductPublicationPublisherDO::getSort)
                .orderByDesc(ProductPublicationPublisherDO::getId));
    }

    int deletePhysicallyById(@Param("id") Long id);
}
