package cn.iocoder.yudao.module.edu.api.publication;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.api.publication.dto.EduPublicationTypeRespDTO;
import cn.iocoder.yudao.module.edu.dal.dataobject.publication.ProductPublicationTypeDO;
import cn.iocoder.yudao.module.edu.service.publication.ProductPublicationTypeService;
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
public class EduPublicationTypeApiImpl implements EduPublicationTypeApi {

    @Resource
    private ProductPublicationTypeService publicationTypeService;

    @Override
    public EduPublicationTypeRespDTO getPublicationType(Long id) {
        ProductPublicationTypeDO publicationType = publicationTypeService.getDO(id);
        return publicationType == null ? null : BeanUtils.toBean(publicationType, EduPublicationTypeRespDTO.class);
    }

    @Override
    public Map<Long, EduPublicationTypeRespDTO> getPublicationTypeMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<EduPublicationTypeRespDTO> publicationTypeList = convertList(
                publicationTypeService.getDOMap(ids).values(),
                item -> BeanUtils.toBean(item, EduPublicationTypeRespDTO.class));
        return convertMap(publicationTypeList, EduPublicationTypeRespDTO::getId);
    }

    @Override
    public List<EduPublicationTypeRespDTO> getEnabledPublicationTypeList() {
        return BeanUtils.toBean(publicationTypeService.getSimpleList(), EduPublicationTypeRespDTO.class);
    }
}
