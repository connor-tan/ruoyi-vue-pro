package cn.iocoder.yudao.module.repo.dal.mysql.supplierpublication;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.repo.controller.admin.supplierpublication.vo.RepoPublicationSkuPageReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.supplierpublication.vo.RepoSupplierPublicationSkuPageReqVO;
import cn.iocoder.yudao.module.repo.dal.dataobject.supplierpublication.RepoSupplierPublicationSkuDO;
import cn.iocoder.yudao.module.repo.service.supplierpublication.bo.RepoPublicationSkuBO;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface RepoSupplierPublicationSkuMapper extends BaseMapperX<RepoSupplierPublicationSkuDO> {

    default PageResult<RepoSupplierPublicationSkuDO> selectPage(RepoSupplierPublicationSkuPageReqVO reqVO) {
        LambdaQueryWrapperX<RepoSupplierPublicationSkuDO> query = new LambdaQueryWrapperX<RepoSupplierPublicationSkuDO>()
                .eqIfPresent(RepoSupplierPublicationSkuDO::getSupplierId, reqVO.getSupplierId())
                .eqIfPresent(RepoSupplierPublicationSkuDO::getSpuId, reqVO.getSpuId())
                .eqIfPresent(RepoSupplierPublicationSkuDO::getSkuId, reqVO.getSkuId())
                .eqIfPresent(RepoSupplierPublicationSkuDO::getStatus, reqVO.getStatus());
        if (StrUtil.isNotBlank(reqVO.getKeyword())) {
            query.and(wrapper -> wrapper
                        .like(RepoSupplierPublicationSkuDO::getProductNameSnapshot, reqVO.getKeyword())
                        .or()
                        .like(RepoSupplierPublicationSkuDO::getProductSkuNameSnapshot, reqVO.getKeyword())
                        .or()
                        .like(RepoSupplierPublicationSkuDO::getIsbn, reqVO.getKeyword()));
        }
        return selectPage(reqVO, query
                .orderByAsc(RepoSupplierPublicationSkuDO::getSort)
                .orderByDesc(RepoSupplierPublicationSkuDO::getId));
    }

    default RepoSupplierPublicationSkuDO selectBySupplierIdAndSkuId(Long supplierId, Long skuId) {
        return selectOne(RepoSupplierPublicationSkuDO::getSupplierId, supplierId,
                RepoSupplierPublicationSkuDO::getSkuId, skuId);
    }

    default List<RepoSupplierPublicationSkuDO> selectListBySupplierId(Long supplierId) {
        return selectList(RepoSupplierPublicationSkuDO::getSupplierId, supplierId);
    }

    default Long selectCountBySupplierId(Long supplierId) {
        return selectCount(RepoSupplierPublicationSkuDO::getSupplierId, supplierId);
    }

    IPage<RepoPublicationSkuBO> selectPublicationSkuPage(IPage<?> page, @Param("reqVO") RepoPublicationSkuPageReqVO reqVO);

    RepoPublicationSkuBO selectPublicationSkuBySkuId(@Param("skuId") Long skuId);

    List<RepoPublicationSkuBO> selectPublicationSkuListBySkuIds(@Param("skuIds") Collection<Long> skuIds);

}
