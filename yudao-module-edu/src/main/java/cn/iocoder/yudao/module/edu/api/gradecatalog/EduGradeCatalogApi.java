package cn.iocoder.yudao.module.edu.api.gradecatalog;

import cn.iocoder.yudao.module.edu.api.gradecatalog.dto.EduGradeCatalogRespDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface EduGradeCatalogApi {

    Map<Long, EduGradeCatalogRespDTO> getGradeCatalogMap(Collection<Long> ids);

    List<EduGradeCatalogRespDTO> getEnabledGradeCatalogList();
}
