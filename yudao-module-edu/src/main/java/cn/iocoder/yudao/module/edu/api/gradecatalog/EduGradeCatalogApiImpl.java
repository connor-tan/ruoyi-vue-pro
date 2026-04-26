package cn.iocoder.yudao.module.edu.api.gradecatalog;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.api.gradecatalog.dto.EduGradeCatalogRespDTO;
import cn.iocoder.yudao.module.edu.service.gradecatalog.GradeCatalogService;
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
public class EduGradeCatalogApiImpl implements EduGradeCatalogApi {

    @Resource
    private GradeCatalogService gradeCatalogService;

    @Override
    public Map<Long, EduGradeCatalogRespDTO> getGradeCatalogMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<EduGradeCatalogRespDTO> gradeCatalogList = convertList(
                gradeCatalogService.getGradeCatalogMap(ids).values(),
                item -> BeanUtils.toBean(item, EduGradeCatalogRespDTO.class));
        return convertMap(gradeCatalogList, EduGradeCatalogRespDTO::getId);
    }

    @Override
    public List<EduGradeCatalogRespDTO> getEnabledGradeCatalogList() {
        return BeanUtils.toBean(gradeCatalogService.getEnabledGradeCatalogList(), EduGradeCatalogRespDTO.class);
    }
}
