package cn.iocoder.yudao.module.product.service.publication;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.product.controller.admin.publicationtitle.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.publication.*;
import cn.iocoder.yudao.module.product.dal.mysql.publication.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ProductPublicationTitleService {

    @Resource
    private ProductPublicationTitleMapper publicationTitleMapper;
    @Resource
    private ProductPublicationTitleIdentifierMapper publicationTitleIdentifierMapper;
    @Resource
    private ProductSpuPublicationMapper productSpuPublicationMapper;
    @Resource
    private ProductPublicationTypeService publicationTypeService;
    @Resource
    private ProductPublicationPublisherService publicationPublisherService;

    @Transactional(rollbackFor = Exception.class)
    public Long create(ProductPublicationTitleSaveReqVO reqVO) {
        publicationTypeService.validateExists(reqVO.getTypeId());
        publicationPublisherService.validateExists(reqVO.getPublisherId());
        validateCodeUnique(null, reqVO.getCode());
        validateNameUnique(null, reqVO.getName());
        ProductPublicationTitleDO title = BeanUtils.toBean(reqVO, ProductPublicationTitleDO.class);
        publicationTitleMapper.insert(title);
        saveIdentifier(title.getId(), reqVO);
        return title.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(ProductPublicationTitleSaveReqVO reqVO) {
        validateExists(reqVO.getId());
        publicationTypeService.validateExists(reqVO.getTypeId());
        publicationPublisherService.validateExists(reqVO.getPublisherId());
        validateCodeUnique(reqVO.getId(), reqVO.getCode());
        validateNameUnique(reqVO.getId(), reqVO.getName());
        publicationTitleMapper.updateById(BeanUtils.toBean(reqVO, ProductPublicationTitleDO.class));
        saveIdentifier(reqVO.getId(), reqVO);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        validateExists(id);
        if (productSpuPublicationMapper.countByPublicationTitleId(id) > 0) {
            throw exception(PUBLICATION_TITLE_HAS_PRODUCTS);
        }
        publicationTitleIdentifierMapper.deleteById(id);
        publicationTitleMapper.deleteById(id);
    }

    public ProductPublicationTitleRespVO get(Long id) {
        ProductPublicationTitleDO title = publicationTitleMapper.selectById(id);
        return title == null ? null : buildResp(title,
                publicationTitleIdentifierMapper.selectByPublicationTitleId(id),
                publicationTypeService.validateExists(title.getTypeId()),
                publicationPublisherService.validateExists(title.getPublisherId()));
    }

    public PageResult<ProductPublicationTitleRespVO> getPage(ProductPublicationTitlePageReqVO reqVO) {
        PageResult<ProductPublicationTitleDO> pageResult = publicationTitleMapper.selectPage(reqVO);
        return new PageResult<>(buildRespList(pageResult.getList()), pageResult.getTotal());
    }

    public List<ProductPublicationTitleSimpleRespVO> getSimpleList() {
        List<ProductPublicationTitleDO> titles = publicationTitleMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
        Map<Long, ProductPublicationTypeDO> typeMap = buildTypeMap(titles);
        Map<Long, ProductPublicationPublisherDO> publisherMap = buildPublisherMap(titles);
        return CollectionUtils.convertList(titles, title -> {
            ProductPublicationTitleSimpleRespVO respVO = BeanUtils.toBean(title, ProductPublicationTitleSimpleRespVO.class);
            ProductPublicationTypeDO type = typeMap.get(title.getTypeId());
            if (type != null) {
                respVO.setTypeCode(type.getCode());
                respVO.setTypeName(type.getName());
            }
            ProductPublicationPublisherDO publisher = publisherMap.get(title.getPublisherId());
            if (publisher != null) {
                respVO.setPublisherName(publisher.getName());
            }
            return respVO;
        });
    }

    public ProductPublicationTitleDO validateExists(Long id) {
        ProductPublicationTitleDO title = publicationTitleMapper.selectById(id);
        if (title == null) {
            throw exception(PUBLICATION_TITLE_NOT_EXISTS);
        }
        return title;
    }

    public Map<Long, ProductPublicationTitleDO> getTitleMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return CollectionUtils.convertMap(publicationTitleMapper.selectByIds(ids), ProductPublicationTitleDO::getId);
    }

    public Set<Long> getTitleIdsByTypeId(Long typeId) {
        if (typeId == null) {
            return Collections.emptySet();
        }
        return CollectionUtils.convertSet(
                publicationTitleMapper.selectListByTypeIds(Collections.singleton(typeId)),
                ProductPublicationTitleDO::getId);
    }

    public Set<Long> getTitleIdsByPublisherId(Long publisherId) {
        if (publisherId == null) {
            return Collections.emptySet();
        }
        return CollectionUtils.convertSet(
                publicationTitleMapper.selectListByPublisherIds(Collections.singleton(publisherId)),
                ProductPublicationTitleDO::getId);
    }

    public Map<Long, ProductPublicationTitleIdentifierDO> getIdentifierMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return CollectionUtils.convertMap(publicationTitleIdentifierMapper.selectListByPublicationTitleIds(ids),
                ProductPublicationTitleIdentifierDO::getPublicationTitleId);
    }

    public boolean requiresPeriodicalIdentifier(Long typeId) {
        String typeCode = publicationTypeService.validateExists(typeId).getCode();
        return "PERIODICAL".equalsIgnoreCase(typeCode) || "NEWSPAPER".equalsIgnoreCase(typeCode);
    }

    private void saveIdentifier(Long titleId, ProductPublicationTitleSaveReqVO reqVO) {
        ProductPublicationTitleIdentifierDO identifier = ProductPublicationTitleIdentifierDO.builder()
                .publicationTitleId(titleId)
                .issn(reqVO.getIssn())
                .cnCode(reqVO.getCnCode())
                .postDistributionCode(reqVO.getPostDistributionCode())
                .build();
        publicationTitleIdentifierMapper.insertOrUpdate(identifier);
    }

    private List<ProductPublicationTitleRespVO> buildRespList(List<ProductPublicationTitleDO> titles) {
        if (titles == null || titles.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, ProductPublicationTitleIdentifierDO> identifierMap = getIdentifierMap(CollectionUtils.convertSet(titles, ProductPublicationTitleDO::getId));
        Map<Long, ProductPublicationTypeDO> typeMap = buildTypeMap(titles);
        Map<Long, ProductPublicationPublisherDO> publisherMap = buildPublisherMap(titles);
        return CollectionUtils.convertList(titles, title ->
                buildResp(title, identifierMap.get(title.getId()), typeMap.get(title.getTypeId()), publisherMap.get(title.getPublisherId())));
    }

    private Map<Long, ProductPublicationTypeDO> buildTypeMap(Collection<ProductPublicationTitleDO> titles) {
        return CollectionUtils.convertMap(publicationTypeService.getSimpleList().stream()
                .map(type -> ProductPublicationTypeDO.builder().id(type.getId()).code(type.getCode()).name(type.getName()).build())
                .toList(), ProductPublicationTypeDO::getId);
    }

    private Map<Long, ProductPublicationPublisherDO> buildPublisherMap(Collection<ProductPublicationTitleDO> titles) {
        Set<Long> publisherIds = CollectionUtils.convertSet(titles, ProductPublicationTitleDO::getPublisherId);
        if (publisherIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ProductPublicationPublisherDO> publishers = publicationPublisherService.getSimpleList().stream()
                .filter(item -> publisherIds.contains(item.getId()))
                .map(item -> ProductPublicationPublisherDO.builder().id(item.getId()).name(item.getName()).code(item.getCode()).build())
                .toList();
        return CollectionUtils.convertMap(publishers, ProductPublicationPublisherDO::getId);
    }

    private ProductPublicationTitleRespVO buildResp(ProductPublicationTitleDO title,
                                                    ProductPublicationTitleIdentifierDO identifier,
                                                    ProductPublicationTypeDO type,
                                                    ProductPublicationPublisherDO publisher) {
        ProductPublicationTitleRespVO respVO = BeanUtils.toBean(title, ProductPublicationTitleRespVO.class);
        if (identifier != null) {
            respVO.setIssn(identifier.getIssn());
            respVO.setCnCode(identifier.getCnCode());
            respVO.setPostDistributionCode(identifier.getPostDistributionCode());
        }
        if (type != null) {
            respVO.setTypeCode(type.getCode());
            respVO.setTypeName(type.getName());
        }
        if (publisher != null) {
            respVO.setPublisherName(publisher.getName());
        }
        return respVO;
    }

    private void validateCodeUnique(Long id, String code) {
        ProductPublicationTitleDO title = publicationTitleMapper.selectByCode(code);
        if (title == null) {
            return;
        }
        if (id == null || !Objects.equals(id, title.getId())) {
            throw exception(PUBLICATION_TITLE_CODE_EXISTS);
        }
    }

    private void validateNameUnique(Long id, String name) {
        ProductPublicationTitleDO title = publicationTitleMapper.selectByName(name);
        if (title == null) {
            return;
        }
        if (id == null || !Objects.equals(id, title.getId())) {
            throw exception(PUBLICATION_TITLE_NAME_EXISTS);
        }
    }
}
