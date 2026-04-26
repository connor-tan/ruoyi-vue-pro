package cn.iocoder.yudao.module.edu.service.publication;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.controller.admin.publicationtype.vo.*;
import cn.iocoder.yudao.module.edu.dal.dataobject.publication.ProductPublicationTypeDO;
import cn.iocoder.yudao.module.edu.dal.mysql.publication.ProductPublicationTypeMapper;
import cn.iocoder.yudao.module.publication.api.enums.PublicationIdentifierRuleEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.PUBLICATION_TYPE_NAME_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.PUBLICATION_TYPE_NOT_EXISTS;

@Service
@Validated
public class ProductPublicationTypeService {

    @Resource
    private ProductPublicationTypeMapper publicationTypeMapper;

    public Long create(ProductPublicationTypeSaveReqVO reqVO) {
        normalizeIdentifierRule(reqVO);
        validateNameUnique(null, reqVO.getName());
        ProductPublicationTypeDO type = BeanUtils.toBean(reqVO, ProductPublicationTypeDO.class);
        publicationTypeMapper.insert(type);
        return type.getId();
    }

    public void update(ProductPublicationTypeSaveReqVO reqVO) {
        validateExists(reqVO.getId());
        normalizeIdentifierRule(reqVO);
        validateNameUnique(reqVO.getId(), reqVO.getName());
        publicationTypeMapper.updateById(BeanUtils.toBean(reqVO, ProductPublicationTypeDO.class));
    }

    public void delete(Long id) {
        validateExists(id);
        publicationTypeMapper.deleteById(id);
    }

    public ProductPublicationTypeRespVO get(Long id) {
        ProductPublicationTypeDO type = publicationTypeMapper.selectById(id);
        return type == null ? null : BeanUtils.toBean(type, ProductPublicationTypeRespVO.class);
    }

    public ProductPublicationTypeDO getDO(Long id) {
        return publicationTypeMapper.selectById(id);
    }

    public Map<Long, ProductPublicationTypeDO> getDOMap(Collection<Long> ids) {
        return convertMap(publicationTypeMapper.selectByIds(ids), ProductPublicationTypeDO::getId);
    }

    public PageResult<ProductPublicationTypeRespVO> getPage(ProductPublicationTypePageReqVO reqVO) {
        return BeanUtils.toBean(publicationTypeMapper.selectPage(reqVO), ProductPublicationTypeRespVO.class);
    }

    public List<ProductPublicationTypeSimpleRespVO> getSimpleList() {
        return BeanUtils.toBean(publicationTypeMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus()),
                ProductPublicationTypeSimpleRespVO.class);
    }

    public ProductPublicationTypeDO validateExists(Long id) {
        ProductPublicationTypeDO type = publicationTypeMapper.selectById(id);
        if (type == null) {
            throw exception(PUBLICATION_TYPE_NOT_EXISTS);
        }
        return type;
    }

    public ProductPublicationTypeDO validateEnabled(Long id) {
        ProductPublicationTypeDO type = validateExists(id);
        if (!CommonStatusEnum.ENABLE.getStatus().equals(type.getStatus())) {
            throw exception(PUBLICATION_TYPE_NOT_EXISTS);
        }
        return type;
    }

    private void validateNameUnique(Long id, String name) {
        ProductPublicationTypeDO type = publicationTypeMapper.selectByName(name);
        if (type == null) {
            return;
        }
        if (id == null || !type.getId().equals(id)) {
            throw exception(PUBLICATION_TYPE_NAME_EXISTS);
        }
    }

    private void normalizeIdentifierRule(ProductPublicationTypeSaveReqVO reqVO) {
        reqVO.setIdentifierRule(PublicationIdentifierRuleEnum.normalize(reqVO.getIdentifierRule()));
    }
}
