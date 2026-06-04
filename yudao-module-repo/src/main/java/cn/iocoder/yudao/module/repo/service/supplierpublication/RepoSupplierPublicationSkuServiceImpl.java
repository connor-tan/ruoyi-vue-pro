package cn.iocoder.yudao.module.repo.service.supplierpublication;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.module.repo.controller.admin.supplierpublication.vo.RepoPublicationSkuPageReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.supplierpublication.vo.RepoSupplierPublicationSkuPageReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.supplierpublication.vo.RepoSupplierPublicationSkuSaveReqVO;
import cn.iocoder.yudao.module.repo.dal.dataobject.supplierpublication.RepoSupplierPublicationSkuDO;
import cn.iocoder.yudao.module.repo.dal.mysql.supplierpublication.RepoSupplierPublicationSkuMapper;
import cn.iocoder.yudao.module.repo.service.supplier.RepoSupplierService;
import cn.iocoder.yudao.module.repo.service.supplierpublication.bo.RepoPublicationSkuBO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.repo.enums.ErrorCodeConstants.PUBLICATION_SKU_NOT_EXISTS;
import static cn.iocoder.yudao.module.repo.enums.ErrorCodeConstants.SUPPLIER_PUBLICATION_SKU_DISABLED;
import static cn.iocoder.yudao.module.repo.enums.ErrorCodeConstants.SUPPLIER_PUBLICATION_SKU_DUPLICATE;
import static cn.iocoder.yudao.module.repo.enums.ErrorCodeConstants.SUPPLIER_PUBLICATION_SKU_NOT_EXISTS;

@Service
@Validated
public class RepoSupplierPublicationSkuServiceImpl implements RepoSupplierPublicationSkuService {

    @Resource
    private RepoSupplierPublicationSkuMapper supplierPublicationSkuMapper;
    @Resource
    private RepoSupplierService supplierService;

    @Override
    public Long createSupplierPublicationSku(RepoSupplierPublicationSkuSaveReqVO createReqVO) {
        supplierService.validateSupplierEnabled(createReqVO.getSupplierId());
        RepoPublicationSkuBO publicationSku = validatePublicationSkuExists(createReqVO.getSkuId());
        validateSupplierPublicationSkuUnique(null, createReqVO.getSupplierId(), createReqVO.getSkuId());
        RepoSupplierPublicationSkuDO relation = BeanUtils.toBean(createReqVO, RepoSupplierPublicationSkuDO.class);
        fillPublicationSkuSnapshot(relation, publicationSku);
        supplierPublicationSkuMapper.insert(relation);
        return relation.getId();
    }

    @Override
    public void updateSupplierPublicationSku(RepoSupplierPublicationSkuSaveReqVO updateReqVO) {
        validateSupplierPublicationSkuExists(updateReqVO.getId());
        supplierService.validateSupplierEnabled(updateReqVO.getSupplierId());
        RepoPublicationSkuBO publicationSku = validatePublicationSkuExists(updateReqVO.getSkuId());
        validateSupplierPublicationSkuUnique(updateReqVO.getId(), updateReqVO.getSupplierId(), updateReqVO.getSkuId());
        RepoSupplierPublicationSkuDO updateObj = BeanUtils.toBean(updateReqVO, RepoSupplierPublicationSkuDO.class);
        fillPublicationSkuSnapshot(updateObj, publicationSku);
        supplierPublicationSkuMapper.updateById(updateObj);
    }

    @Override
    public void deleteSupplierPublicationSku(Long id) {
        validateSupplierPublicationSkuExists(id);
        supplierPublicationSkuMapper.deleteById(id);
    }

    @Override
    public RepoSupplierPublicationSkuDO getSupplierPublicationSku(Long id) {
        return supplierPublicationSkuMapper.selectById(id);
    }

    @Override
    public PageResult<RepoSupplierPublicationSkuDO> getSupplierPublicationSkuPage(
            RepoSupplierPublicationSkuPageReqVO pageReqVO) {
        return supplierPublicationSkuMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<RepoPublicationSkuBO> getPublicationSkuPage(RepoPublicationSkuPageReqVO pageReqVO) {
        IPage<RepoPublicationSkuBO> page = supplierPublicationSkuMapper.selectPublicationSkuPage(
                MyBatisUtils.buildPage(pageReqVO), pageReqVO);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public RepoPublicationSkuBO getPublicationSku(Long skuId) {
        return supplierPublicationSkuMapper.selectPublicationSkuBySkuId(skuId);
    }

    @Override
    public Map<Long, RepoPublicationSkuBO> getPublicationSkuMap(Collection<Long> skuIds) {
        if (CollUtil.isEmpty(skuIds)) {
            return Collections.emptyMap();
        }
        List<Long> filteredSkuIds = skuIds.stream().filter(Objects::nonNull).distinct().toList();
        if (CollUtil.isEmpty(filteredSkuIds)) {
            return Collections.emptyMap();
        }
        return convertMap(supplierPublicationSkuMapper.selectPublicationSkuListBySkuIds(filteredSkuIds),
                RepoPublicationSkuBO::getSkuId);
    }

    @Override
    public RepoSupplierPublicationSkuDO validateSupplierPublicationSkuEnabled(Long supplierId, Long skuId) {
        RepoSupplierPublicationSkuDO relation = supplierPublicationSkuMapper.selectBySupplierIdAndSkuId(supplierId, skuId);
        if (relation == null) {
            throw exception(SUPPLIER_PUBLICATION_SKU_NOT_EXISTS);
        }
        if (CommonStatusEnum.isDisable(relation.getStatus())) {
            throw exception(SUPPLIER_PUBLICATION_SKU_DISABLED);
        }
        return relation;
    }

    @Override
    public List<RepoSupplierPublicationSkuDO> getSupplierPublicationSkuListBySupplierId(Long supplierId) {
        return supplierPublicationSkuMapper.selectListBySupplierId(supplierId);
    }

    private RepoSupplierPublicationSkuDO validateSupplierPublicationSkuExists(Long id) {
        RepoSupplierPublicationSkuDO relation = supplierPublicationSkuMapper.selectById(id);
        if (relation == null) {
            throw exception(SUPPLIER_PUBLICATION_SKU_NOT_EXISTS);
        }
        return relation;
    }

    private RepoPublicationSkuBO validatePublicationSkuExists(Long skuId) {
        RepoPublicationSkuBO publicationSku = supplierPublicationSkuMapper.selectPublicationSkuBySkuId(skuId);
        if (publicationSku == null) {
            throw exception(PUBLICATION_SKU_NOT_EXISTS);
        }
        return publicationSku;
    }

    private void validateSupplierPublicationSkuUnique(Long id, Long supplierId, Long skuId) {
        RepoSupplierPublicationSkuDO existed = supplierPublicationSkuMapper.selectBySupplierIdAndSkuId(supplierId, skuId);
        if (existed == null || Objects.equals(existed.getId(), id)) {
            return;
        }
        throw exception(SUPPLIER_PUBLICATION_SKU_DUPLICATE);
    }

    private void fillPublicationSkuSnapshot(RepoSupplierPublicationSkuDO relation, RepoPublicationSkuBO publicationSku) {
        relation.setSpuId(publicationSku.getSpuId())
                .setProductNameSnapshot(publicationSku.getProductName())
                .setProductSkuNameSnapshot(publicationSku.getProductSkuName())
                .setIsbn(publicationSku.getIsbn());
    }

}
