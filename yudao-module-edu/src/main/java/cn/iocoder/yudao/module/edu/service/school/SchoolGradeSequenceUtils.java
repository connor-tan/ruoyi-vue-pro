package cn.iocoder.yudao.module.edu.service.school;

import cn.iocoder.yudao.module.edu.dal.dataobject.school.GradeCatalogDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolGradeDO;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 学校年级顺序工具。
 */
public final class SchoolGradeSequenceUtils {

    private SchoolGradeSequenceUtils() {
    }

    public static Map<Long, Long> buildNextGradeCatalogIdMap(List<GradeCatalogDO> enabledGradeCatalogs) {
        Map<Long, Long> nextMap = new HashMap<>();
        for (int i = 0; i < enabledGradeCatalogs.size(); i++) {
            GradeCatalogDO current = enabledGradeCatalogs.get(i);
            GradeCatalogDO next = i + 1 < enabledGradeCatalogs.size() ? enabledGradeCatalogs.get(i + 1) : null;
            nextMap.put(current.getId(), next == null ? null : next.getId());
        }
        return nextMap;
    }

    public static Map<Long, SchoolGradeDO> buildNextSchoolGradeMap(List<SchoolGradeDO> schoolGrades,
                                                                   Map<Long, GradeCatalogDO> gradeCatalogMap) {
        List<SchoolGradeDO> sortedSchoolGrades = schoolGrades.stream()
                .filter(schoolGrade -> gradeCatalogMap.containsKey(schoolGrade.getGradeCatalogId()))
                .sorted(Comparator
                        .comparing((SchoolGradeDO item) -> gradeCatalogMap.get(item.getGradeCatalogId()).getSort())
                        .thenComparing(SchoolGradeDO::getId))
                .collect(Collectors.toList());
        Map<Long, SchoolGradeDO> nextMap = new HashMap<>();
        for (int i = 0; i < sortedSchoolGrades.size(); i++) {
            SchoolGradeDO current = sortedSchoolGrades.get(i);
            SchoolGradeDO next = i + 1 < sortedSchoolGrades.size() ? sortedSchoolGrades.get(i + 1) : null;
            nextMap.put(current.getId(), next);
        }
        return nextMap;
    }

    public static Set<Long> buildFirstGradeCatalogIdSet(List<GradeCatalogDO> enabledGradeCatalogs) {
        return enabledGradeCatalogs.stream()
                .collect(Collectors.groupingBy(GradeCatalogDO::getStage, LinkedHashMap::new,
                        Collectors.minBy(Comparator
                                .comparing(GradeCatalogDO::getSort)
                                .thenComparing(GradeCatalogDO::getId))))
                .values().stream()
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(GradeCatalogDO::getId)
                .collect(Collectors.toSet());
    }

}
