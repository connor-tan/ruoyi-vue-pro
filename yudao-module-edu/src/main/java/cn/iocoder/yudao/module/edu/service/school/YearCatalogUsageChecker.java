package cn.iocoder.yudao.module.edu.service.school;

/**
 * 全局学年目录使用方检查器
 */
public interface YearCatalogUsageChecker {

    /**
     * 统计指定学年目录的使用量。
     *
     * @param yearCatalogId 学年目录编号
     * @return 使用量
     */
    long countUsage(Long yearCatalogId);
}
