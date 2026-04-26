package cn.iocoder.yudao.module.edu.api.publication;

import cn.iocoder.yudao.module.edu.api.publication.dto.EduPublicationPublisherRespDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface EduPublicationPublisherApi {

    EduPublicationPublisherRespDTO getPublicationPublisher(Long id);

    Map<Long, EduPublicationPublisherRespDTO> getPublicationPublisherMap(Collection<Long> ids);

    List<EduPublicationPublisherRespDTO> getEnabledPublicationPublisherList();
}
