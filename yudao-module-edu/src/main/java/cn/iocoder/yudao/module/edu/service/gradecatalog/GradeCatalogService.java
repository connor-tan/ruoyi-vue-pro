package cn.iocoder.yudao.module.edu.service.gradecatalog;

import cn.iocoder.yudao.module.edu.dal.dataobject.school.GradeCatalogDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface GradeCatalogService {

    Map<Long, GradeCatalogDO> getGradeCatalogMap(Collection<Long> ids);

    List<GradeCatalogDO> getEnabledGradeCatalogList();
}
