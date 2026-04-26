package cn.iocoder.yudao.module.edu.service.gradecatalog;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.GradeCatalogDO;
import cn.iocoder.yudao.module.edu.dal.mysql.school.GradeCatalogMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

@Service
@Validated
public class GradeCatalogServiceImpl implements GradeCatalogService {

    @Resource
    private GradeCatalogMapper gradeCatalogMapper;

    @Override
    public Map<Long, GradeCatalogDO> getGradeCatalogMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(gradeCatalogMapper.selectByIds(ids), GradeCatalogDO::getId);
    }

    @Override
    public List<GradeCatalogDO> getEnabledGradeCatalogList() {
        return gradeCatalogMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }
}
