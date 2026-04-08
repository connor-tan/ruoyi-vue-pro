package cn.iocoder.yudao.module.product.service.publication;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.product.controller.admin.publicationpublisher.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.ProductPublicationPublisherDO;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductPublicationTitleMapper;
import cn.iocoder.yudao.module.product.dal.mysql.publication.ProductPublicationPublisherMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ProductPublicationPublisherService {

    @Resource
    private ProductPublicationPublisherMapper publicationPublisherMapper;
    @Resource
    private ProductPublicationTitleMapper publicationTitleMapper;

    public Long create(ProductPublicationPublisherSaveReqVO reqVO) {
        validateCodeUnique(null, reqVO.getCode());
        validateNameUnique(null, reqVO.getName());
        ProductPublicationPublisherDO publisher = BeanUtils.toBean(reqVO, ProductPublicationPublisherDO.class);
        publicationPublisherMapper.insert(publisher);
        return publisher.getId();
    }

    public void update(ProductPublicationPublisherSaveReqVO reqVO) {
        validateExists(reqVO.getId());
        validateCodeUnique(reqVO.getId(), reqVO.getCode());
        validateNameUnique(reqVO.getId(), reqVO.getName());
        publicationPublisherMapper.updateById(BeanUtils.toBean(reqVO, ProductPublicationPublisherDO.class));
    }

    public void delete(Long id) {
        validateExists(id);
        if (publicationTitleMapper.countByPublisherId(id) > 0) {
            throw exception(PUBLICATION_PUBLISHER_HAS_TITLES);
        }
        publicationPublisherMapper.deleteById(id);
    }

    public ProductPublicationPublisherRespVO get(Long id) {
        ProductPublicationPublisherDO publisher = publicationPublisherMapper.selectById(id);
        return publisher == null ? null : BeanUtils.toBean(publisher, ProductPublicationPublisherRespVO.class);
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

    private void validateCodeUnique(Long id, String code) {
        ProductPublicationPublisherDO publisher = publicationPublisherMapper.selectByCode(code);
        if (publisher == null) {
            return;
        }
        if (id == null || !publisher.getId().equals(id)) {
            throw exception(PUBLICATION_PUBLISHER_CODE_EXISTS);
        }
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
