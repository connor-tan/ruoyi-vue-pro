package cn.iocoder.yudao.module.edu.api.yearcatalog;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.api.yearcatalog.dto.EduYearCatalogRespDTO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.YearCatalogDO;
import cn.iocoder.yudao.module.edu.service.yearcatalog.YearCatalogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class EduYearCatalogApiImpl implements EduYearCatalogApi {

    @Resource
    private YearCatalogService yearCatalogService;

    @Override
    public EduYearCatalogRespDTO getYearCatalog(Long id) {
        YearCatalogDO yearCatalog = yearCatalogService.validateYearCatalogExists(id);
        EduYearCatalogRespDTO respDTO = BeanUtils.toBean(yearCatalog, EduYearCatalogRespDTO.class);
        respDTO.setName(buildYearCatalogName(yearCatalog.getYearStart(), yearCatalog.getYearEnd()));
        return respDTO;
    }

    private String buildYearCatalogName(Integer yearStart, Integer yearEnd) {
        return yearStart == null || yearEnd == null ? null : yearStart + "-" + yearEnd + "学年";
    }

}
