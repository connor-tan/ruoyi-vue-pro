package cn.iocoder.yudao.module.edu.api.publication;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.api.publication.dto.EduPublicationPublisherRespDTO;
import cn.iocoder.yudao.module.edu.dal.dataobject.publication.ProductPublicationPublisherDO;
import cn.iocoder.yudao.module.edu.service.publication.ProductPublicationPublisherService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

@Service
@Validated
public class EduPublicationPublisherApiImpl implements EduPublicationPublisherApi {

    @Resource
    private ProductPublicationPublisherService publicationPublisherService;

    @Override
    public EduPublicationPublisherRespDTO getPublicationPublisher(Long id) {
        ProductPublicationPublisherDO publisher = publicationPublisherService.getDO(id);
        return publisher == null ? null : BeanUtils.toBean(publisher, EduPublicationPublisherRespDTO.class);
    }

    @Override
    public Map<Long, EduPublicationPublisherRespDTO> getPublicationPublisherMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<EduPublicationPublisherRespDTO> publisherList = convertList(
                publicationPublisherService.getDOMap(ids).values(),
                item -> BeanUtils.toBean(item, EduPublicationPublisherRespDTO.class));
        return convertMap(publisherList, EduPublicationPublisherRespDTO::getId);
    }

    @Override
    public List<EduPublicationPublisherRespDTO> getEnabledPublicationPublisherList() {
        return BeanUtils.toBean(publicationPublisherService.getSimpleList(), EduPublicationPublisherRespDTO.class);
    }
}
