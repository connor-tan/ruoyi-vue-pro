package cn.iocoder.yudao.module.edu.service.school;

import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolYearMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class SchoolYearCatalogUsageChecker implements YearCatalogUsageChecker {

    @Resource
    private SchoolYearMapper schoolYearMapper;

    @Override
    public long countUsage(Long yearCatalogId) {
        return schoolYearMapper.countByYearCatalogId(yearCatalogId);
    }
}
