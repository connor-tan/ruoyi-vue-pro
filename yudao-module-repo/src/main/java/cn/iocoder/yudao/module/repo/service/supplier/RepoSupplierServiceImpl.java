package cn.iocoder.yudao.module.repo.service.supplier;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.repo.controller.admin.supplier.vo.RepoSupplierPageReqVO;
import cn.iocoder.yudao.module.repo.controller.admin.supplier.vo.RepoSupplierSaveReqVO;
import cn.iocoder.yudao.module.repo.dal.dataobject.publicationreceipt.RepoPublicationReceiptDO;
import cn.iocoder.yudao.module.repo.dal.dataobject.supplier.RepoSupplierDO;
import cn.iocoder.yudao.module.repo.dal.mysql.publicationreceipt.RepoPublicationReceiptMapper;
import cn.iocoder.yudao.module.repo.dal.mysql.supplier.RepoSupplierMapper;
import cn.iocoder.yudao.module.repo.dal.mysql.supplierpublication.RepoSupplierPublicationSkuMapper;
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
import static cn.iocoder.yudao.module.repo.enums.ErrorCodeConstants.SUPPLIER_CODE_DUPLICATE;
import static cn.iocoder.yudao.module.repo.enums.ErrorCodeConstants.SUPPLIER_DISABLED;
import static cn.iocoder.yudao.module.repo.enums.ErrorCodeConstants.SUPPLIER_IN_USE;
import static cn.iocoder.yudao.module.repo.enums.ErrorCodeConstants.SUPPLIER_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.repo.enums.ErrorCodeConstants.SUPPLIER_NOT_EXISTS;

@Service
@Validated
public class RepoSupplierServiceImpl implements RepoSupplierService {

    @Resource
    private RepoSupplierMapper supplierMapper;
    @Resource
    private RepoSupplierPublicationSkuMapper supplierPublicationSkuMapper;
    @Resource
    private RepoPublicationReceiptMapper publicationReceiptMapper;

    @Override
    public Long createSupplier(RepoSupplierSaveReqVO createReqVO) {
        validateSupplierNameUnique(null, createReqVO.getName());
        validateSupplierCodeUnique(null, createReqVO.getCode());
        RepoSupplierDO supplier = BeanUtils.toBean(createReqVO, RepoSupplierDO.class);
        supplierMapper.insert(supplier);
        return supplier.getId();
    }

    @Override
    public void updateSupplier(RepoSupplierSaveReqVO updateReqVO) {
        validateSupplierExists(updateReqVO.getId());
        validateSupplierNameUnique(updateReqVO.getId(), updateReqVO.getName());
        validateSupplierCodeUnique(updateReqVO.getId(), updateReqVO.getCode());
        supplierMapper.updateById(BeanUtils.toBean(updateReqVO, RepoSupplierDO.class));
    }

    @Override
    public void deleteSupplier(Long id) {
        validateSupplierExists(id);
        if (supplierPublicationSkuMapper.selectCountBySupplierId(id) > 0
                || publicationReceiptMapper.selectCount(RepoPublicationReceiptDO::getSupplierId, id) > 0) {
            throw exception(SUPPLIER_IN_USE);
        }
        supplierMapper.deleteById(id);
    }

    @Override
    public RepoSupplierDO getSupplier(Long id) {
        return supplierMapper.selectById(id);
    }

    @Override
    public PageResult<RepoSupplierDO> getSupplierPage(RepoSupplierPageReqVO pageReqVO) {
        return supplierMapper.selectPage(pageReqVO);
    }

    @Override
    public List<RepoSupplierDO> getSupplierListByStatus(Integer status) {
        return supplierMapper.selectListByStatus(status);
    }

    @Override
    public Map<Long, RepoSupplierDO> getSupplierMap(Collection<Long> supplierIds) {
        if (CollUtil.isEmpty(supplierIds)) {
            return Collections.emptyMap();
        }
        List<Long> filteredSupplierIds = supplierIds.stream().filter(Objects::nonNull).distinct().toList();
        if (CollUtil.isEmpty(filteredSupplierIds)) {
            return Collections.emptyMap();
        }
        return convertMap(supplierMapper.selectByIds(filteredSupplierIds), RepoSupplierDO::getId);
    }

    @Override
    public RepoSupplierDO validateSupplierEnabled(Long supplierId) {
        RepoSupplierDO supplier = validateSupplierExists(supplierId);
        if (CommonStatusEnum.isDisable(supplier.getStatus())) {
            throw exception(SUPPLIER_DISABLED);
        }
        return supplier;
    }

    private RepoSupplierDO validateSupplierExists(Long id) {
        RepoSupplierDO supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw exception(SUPPLIER_NOT_EXISTS);
        }
        return supplier;
    }

    private void validateSupplierNameUnique(Long id, String name) {
        RepoSupplierDO existed = supplierMapper.selectByName(name);
        if (existed == null || Objects.equals(existed.getId(), id)) {
            return;
        }
        throw exception(SUPPLIER_NAME_DUPLICATE);
    }

    private void validateSupplierCodeUnique(Long id, String code) {
        if (StrUtil.isBlank(code)) {
            return;
        }
        RepoSupplierDO existed = supplierMapper.selectByCode(code);
        if (existed == null || Objects.equals(existed.getId(), id)) {
            return;
        }
        throw exception(SUPPLIER_CODE_DUPLICATE);
    }

}
