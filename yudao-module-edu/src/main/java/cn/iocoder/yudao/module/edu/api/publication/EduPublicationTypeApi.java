package cn.iocoder.yudao.module.edu.api.publication;

import cn.iocoder.yudao.module.edu.api.publication.dto.EduPublicationTypeRespDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface EduPublicationTypeApi {

    EduPublicationTypeRespDTO getPublicationType(Long id);

    Map<Long, EduPublicationTypeRespDTO> getPublicationTypeMap(Collection<Long> ids);

    List<EduPublicationTypeRespDTO> getEnabledPublicationTypeList();
}
