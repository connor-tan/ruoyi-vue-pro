package cn.iocoder.yudao.module.edu.service.publication;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.controller.admin.publicationpublisher.vo.*;
import cn.iocoder.yudao.module.edu.dal.dataobject.publication.ProductPublicationPublisherDO;
import cn.iocoder.yudao.module.edu.dal.mysql.publication.ProductPublicationPublisherMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.PUBLICATION_PUBLISHER_NAME_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.PUBLICATION_PUBLISHER_NOT_EXISTS;

@Service
@Validated
public class ProductPublicationPublisherService {

    @Resource
    private ProductPublicationPublisherMapper publicationPublisherMapper;

    public Long create(ProductPublicationPublisherSaveReqVO reqVO) {
        validateNameUnique(null, reqVO.getName());
        ProductPublicationPublisherDO publisher = BeanUtils.toBean(reqVO, ProductPublicationPublisherDO.class);
        publicationPublisherMapper.insert(publisher);
        return publisher.getId();
    }

    public void update(ProductPublicationPublisherSaveReqVO reqVO) {
        validateExists(reqVO.getId());
        validateNameUnique(reqVO.getId(), reqVO.getName());
        publicationPublisherMapper.updateById(BeanUtils.toBean(reqVO, ProductPublicationPublisherDO.class));
    }

    public void delete(Long id) {
        validateExists(id);
        publicationPublisherMapper.deleteById(id);
    }

    public ProductPublicationPublisherRespVO get(Long id) {
        ProductPublicationPublisherDO publisher = publicationPublisherMapper.selectById(id);
        return publisher == null ? null : BeanUtils.toBean(publisher, ProductPublicationPublisherRespVO.class);
    }

    public ProductPublicationPublisherDO getDO(Long id) {
        return publicationPublisherMapper.selectById(id);
    }

    public Map<Long, ProductPublicationPublisherDO> getDOMap(Collection<Long> ids) {
        return convertMap(publicationPublisherMapper.selectByIds(ids), ProductPublicationPublisherDO::getId);
    }

    public PageResult<ProductPublicationPublisherRespVO> getPage(ProductPublicationPublisherPageReqVO reqVO) {
        return BeanUtils.toBean(publicationPublisherMapper.selectPage(reqVO), ProductPublicationPublisherRespVO.class);
    }

    public List<ProductPublicationPublisherSimpleRespVO> getSimpleList() {
        return BeanUtils.toBean(
                publicationPublisherMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus()),
                ProductPublicationPublisherSimpleRespVO.class);
    }

    public ProductPublicationPublisherDO validateExists(Long id) {
        ProductPublicationPublisherDO publisher = publicationPublisherMapper.selectById(id);
        if (publisher == null) {
            throw exception(PUBLICATION_PUBLISHER_NOT_EXISTS);
        }
        return publisher;
    }

    public ProductPublicationPublisherDO validateEnabled(Long id) {
        ProductPublicationPublisherDO publisher = validateExists(id);
        if (!CommonStatusEnum.ENABLE.getStatus().equals(publisher.getStatus())) {
            throw exception(PUBLICATION_PUBLISHER_NOT_EXISTS);
        }
        return publisher;
    }

    private void validateNameUnique(Long id, String name) {
        ProductPublicationPublisherDO publisher = publicationPublisherMapper.selectByName(name);
        if (publisher == null) {
            return;
        }
        if (id == null || !publisher.getId().equals(id)) {
            throw exception(PUBLICATION_PUBLISHER_NAME_EXISTS);
        }
    }
}
