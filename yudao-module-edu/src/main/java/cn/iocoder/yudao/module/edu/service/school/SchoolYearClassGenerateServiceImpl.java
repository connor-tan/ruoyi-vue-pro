package cn.iocoder.yudao.module.edu.service.school;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.GradeCatalogDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolClassDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolGradeDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolYearDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.YearCatalogDO;
import cn.iocoder.yudao.module.edu.dal.mysql.school.GradeCatalogMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolClassMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolGradeMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolYearMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.YearCatalogMapper;
import cn.iocoder.yudao.module.edu.service.school.bo.SchoolYearClassGenerateReqBO;
import cn.iocoder.yudao.module.edu.service.school.bo.SchoolYearClassGenerateRespBO;
import com.baomidou.dynamic.datasource.annotation.Master;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 学年班级自动生成 Service 实现类
 */
@Service
@Validated
public class SchoolYearClassGenerateServiceImpl implements SchoolYearClassGenerateService {

    private static final int BATCH_WRITE_SIZE = 500;

    private static final String REASON_SOURCE_YEAR_NOT_FOUND = "SOURCE_YEAR_NOT_FOUND";
    private static final String REASON_SOURCE_YEAR_DATE_MISSING = "SOURCE_YEAR_DATE_MISSING";
    private static final String REASON_SOURCE_GRADE_NOT_FOUND = "SOURCE_GRADE_NOT_FOUND";
    private static final String REASON_TARGET_GRADE_NOT_FOUND = "TARGET_GRADE_NOT_FOUND";
    private static final String REASON_GRADE_SEQUENCE_GAP = "GRADE_SEQUENCE_GAP";
    private static final String REASON_TARGET_CLASS_EXISTS = "TARGET_CLASS_EXISTS";

    @Resource
    private SchoolMapper schoolMapper;
    @Resource
    private SchoolYearMapper schoolYearMapper;
    @Resource
    private YearCatalogMapper yearCatalogMapper;
    @Resource
    private SchoolClassMapper schoolClassMapper;
    @Resource
    private SchoolGradeMapper schoolGradeMapper;
    @Resource
    private GradeCatalogMapper gradeCatalogMapper;

    @Override
    @Master
    @Transactional(rollbackFor = Exception.class)
    public SchoolYearClassGenerateRespBO generate(SchoolYearClassGenerateReqBO reqBO) {
        SchoolYearClassGenerateRequest request = normalizeRequest(reqBO);
        SchoolYearClassGenerateRespBO respBO = buildInitialResp(request);

        List<SchoolDO> schools = schoolMapper.selectList();
        respBO.setProcessedSchoolCount(schools.size());
        if (CollUtil.isEmpty(schools)) {
            return respBO;
        }

        List<GradeCatalogDO> enabledGradeCatalogs = gradeCatalogMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
        Map<Long, GradeCatalogDO> gradeCatalogMap = enabledGradeCatalogs.stream()
                .collect(Collectors.toMap(GradeCatalogDO::getId, Function.identity(), (item1, item2) -> item1));
        Map<Long, Long> nextGradeCatalogIdMap = SchoolGradeSequenceUtils.buildNextGradeCatalogIdMap(enabledGradeCatalogs);
        Set<Long> firstGradeCatalogIds = SchoolGradeSequenceUtils.buildFirstGradeCatalogIdSet(enabledGradeCatalogs);

        List<Long> schoolIds = convertList(schools, SchoolDO::getId);
        Map<Long, Map<Integer, SchoolYearDO>> schoolYearMap = schoolYearMapper.selectListBySchoolIdsAndYearStarts(
                        schoolIds, List.of(request.sourceYearStart, request.targetYearStart)).stream()
                .collect(Collectors.groupingBy(SchoolYearDO::getSchoolId, LinkedHashMap::new,
                        Collectors.toMap(SchoolYearDO::getYearStart, Function.identity(), (item1, item2) -> item1,
                                LinkedHashMap::new)));
        YearCatalogDO targetYearCatalog = getOrCreateYearCatalog(request);

        Map<Long, SchoolYearDO> sourceYearMap = new LinkedHashMap<>();
        Map<Long, SchoolYearDO> targetYearMap = new LinkedHashMap<>();
        for (SchoolDO school : schools) {
            Map<Integer, SchoolYearDO> yearMap = schoolYearMap.getOrDefault(school.getId(), Collections.emptyMap());
            SchoolYearDO sourceYear = yearMap.get(request.sourceYearStart);
            if (sourceYear == null) {
                increaseSkippedSchool(respBO, REASON_SOURCE_YEAR_NOT_FOUND);
                continue;
            }
            if (sourceYear.getStartDate() == null || sourceYear.getEndDate() == null) {
                increaseSkippedSchool(respBO, REASON_SOURCE_YEAR_DATE_MISSING);
                continue;
            }
            sourceYearMap.put(school.getId(), sourceYear);
            SchoolYearDO targetYear = yearMap.get(request.targetYearStart);
            if (targetYear == null) {
                targetYear = buildTargetYear(school.getId(), sourceYear, targetYearCatalog, request);
                respBO.setCreatedYearCount(respBO.getCreatedYearCount() + 1);
                if (request.dryRun) {
                    targetYear.setId(buildDryRunYearId(school.getId(), request.targetYearStart));
                } else {
                    targetYear.clean();
                    schoolYearMapper.insert(targetYear);
                    if (targetYear.getId() == null) {
                        throw new IllegalStateException("SchoolYear id is null after insert");
                    }
                }
            }
            targetYearMap.put(school.getId(), targetYear);
        }
        if (sourceYearMap.isEmpty()) {
            return respBO;
        }

        List<Long> eligibleSchoolIds = List.copyOf(sourceYearMap.keySet());
        Map<Long, List<SchoolGradeDO>> schoolGradeListMap = schoolGradeMapper.selectListBySchoolIds(eligibleSchoolIds)
                .stream()
                .collect(Collectors.groupingBy(SchoolGradeDO::getSchoolId));
        Map<Long, SchoolGradeDO> schoolGradeMap = schoolGradeListMap.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(SchoolGradeDO::getId, Function.identity(), (item1, item2) -> item1));

        Map<Long, List<SchoolClassDO>> sourceClassMap = schoolClassMapper.selectListBySchoolYearIds(
                        convertList(sourceYearMap.values(), SchoolYearDO::getId)).stream()
                .collect(Collectors.groupingBy(SchoolClassDO::getSchoolId));
        Set<String> targetClassKeys = schoolClassMapper.selectListBySchoolYearIds(
                        convertList(targetYearMap.values(), SchoolYearDO::getId)).stream()
                .map(this::buildClassKey)
                .collect(Collectors.toCollection(HashSet::new));

        List<SchoolClassDO> classesToCreate = eligibleSchoolIds.stream()
                .flatMap(schoolId -> buildTargetClasses(schoolId, sourceClassMap.getOrDefault(schoolId, Collections.emptyList()),
                        schoolGradeListMap.getOrDefault(schoolId, Collections.emptyList()), schoolGradeMap,
                        gradeCatalogMap, nextGradeCatalogIdMap, firstGradeCatalogIds, targetYearMap.get(schoolId),
                        targetClassKeys, request, respBO).stream())
                .collect(Collectors.toList());
        respBO.setCreatedClassCount(classesToCreate.size());
        if (!request.dryRun && CollUtil.isNotEmpty(classesToCreate)) {
            schoolClassMapper.insertBatch(classesToCreate, BATCH_WRITE_SIZE);
        }
        return respBO;
    }

    private List<SchoolClassDO> buildTargetClasses(Long schoolId, List<SchoolClassDO> sourceClasses,
                                                   List<SchoolGradeDO> schoolGrades,
                                                   Map<Long, SchoolGradeDO> schoolGradeMap,
                                                   Map<Long, GradeCatalogDO> gradeCatalogMap,
                                                   Map<Long, Long> nextGradeCatalogIdMap,
                                                   Set<Long> firstGradeCatalogIds,
                                                   SchoolYearDO targetYear,
                                                   Set<String> targetClassKeys,
                                                   SchoolYearClassGenerateRequest request,
                                                   SchoolYearClassGenerateRespBO respBO) {
        if (CollUtil.isEmpty(sourceClasses)) {
            return Collections.emptyList();
        }
        Map<Long, SchoolGradeDO> nextSchoolGradeMap = SchoolGradeSequenceUtils.buildNextSchoolGradeMap(
                schoolGrades, gradeCatalogMap);
        List<SchoolClassDO> classesToCreate = sourceClasses.stream()
                .map(sourceClass -> buildPromotedClass(sourceClass, schoolGradeMap, gradeCatalogMap,
                        nextGradeCatalogIdMap, nextSchoolGradeMap, targetYear, targetClassKeys, respBO))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        sourceClasses.stream()
                .filter(sourceClass -> isFirstGradeClass(sourceClass, schoolGradeMap, firstGradeCatalogIds))
                .map(sourceClass -> buildNewEntryClass(schoolId, sourceClass, schoolGradeMap, gradeCatalogMap,
                        targetYear, targetClassKeys, request.targetYearStart, respBO))
                .filter(Objects::nonNull)
                .forEach(classesToCreate::add);
        return classesToCreate;
    }

    private SchoolClassDO buildPromotedClass(SchoolClassDO sourceClass,
                                             Map<Long, SchoolGradeDO> schoolGradeMap,
                                             Map<Long, GradeCatalogDO> gradeCatalogMap,
                                             Map<Long, Long> nextGradeCatalogIdMap,
                                             Map<Long, SchoolGradeDO> nextSchoolGradeMap,
                                             SchoolYearDO targetYear,
                                             Set<String> targetClassKeys,
                                             SchoolYearClassGenerateRespBO respBO) {
        SchoolGradeDO sourceSchoolGrade = schoolGradeMap.get(sourceClass.getSchoolGradeId());
        GradeCatalogDO sourceGradeCatalog = sourceSchoolGrade == null ? null
                : gradeCatalogMap.get(sourceSchoolGrade.getGradeCatalogId());
        if (sourceSchoolGrade == null || sourceGradeCatalog == null) {
            increaseSkippedClass(respBO, REASON_SOURCE_GRADE_NOT_FOUND);
            return null;
        }

        Long expectedNextGradeCatalogId = nextGradeCatalogIdMap.get(sourceGradeCatalog.getId());
        SchoolGradeDO targetSchoolGrade = nextSchoolGradeMap.get(sourceSchoolGrade.getId());
        if (targetSchoolGrade == null || expectedNextGradeCatalogId == null) {
            increaseSkippedClass(respBO, REASON_TARGET_GRADE_NOT_FOUND);
            return null;
        }
        if (!Objects.equals(targetSchoolGrade.getGradeCatalogId(), expectedNextGradeCatalogId)) {
            increaseSkippedClass(respBO, REASON_GRADE_SEQUENCE_GAP);
            return null;
        }
        GradeCatalogDO targetGradeCatalog = gradeCatalogMap.get(targetSchoolGrade.getGradeCatalogId());
        if (targetGradeCatalog == null) {
            increaseSkippedClass(respBO, REASON_TARGET_GRADE_NOT_FOUND);
            return null;
        }
        return buildClassIfAbsent(sourceClass.getSchoolId(), sourceClass.getEntryYear(), targetYear.getId(),
                targetSchoolGrade, targetGradeCatalog, sourceClass.getClassNo(), targetClassKeys, respBO);
    }

    private SchoolClassDO buildNewEntryClass(Long schoolId,
                                             SchoolClassDO sourceClass,
                                             Map<Long, SchoolGradeDO> schoolGradeMap,
                                             Map<Long, GradeCatalogDO> gradeCatalogMap,
                                             SchoolYearDO targetYear,
                                             Set<String> targetClassKeys,
                                             Integer targetYearStart,
                                             SchoolYearClassGenerateRespBO respBO) {
        SchoolGradeDO sourceSchoolGrade = schoolGradeMap.get(sourceClass.getSchoolGradeId());
        GradeCatalogDO sourceGradeCatalog = sourceSchoolGrade == null ? null
                : gradeCatalogMap.get(sourceSchoolGrade.getGradeCatalogId());
        if (sourceSchoolGrade == null || sourceGradeCatalog == null) {
            increaseSkippedClass(respBO, REASON_SOURCE_GRADE_NOT_FOUND);
            return null;
        }
        return buildClassIfAbsent(schoolId, targetYearStart, targetYear.getId(),
                sourceSchoolGrade, sourceGradeCatalog, sourceClass.getClassNo(), targetClassKeys, respBO);
    }

    private SchoolClassDO buildClassIfAbsent(Long schoolId, Integer entryYear, Long schoolYearId,
                                             SchoolGradeDO schoolGrade, GradeCatalogDO gradeCatalog,
                                             Integer classNo, Set<String> targetClassKeys,
                                             SchoolYearClassGenerateRespBO respBO) {
        String classKey = buildClassKey(schoolId, entryYear, schoolYearId, schoolGrade.getId(), classNo);
        if (!targetClassKeys.add(classKey)) {
            increaseSkippedClass(respBO, REASON_TARGET_CLASS_EXISTS);
            return null;
        }
        SchoolClassDO schoolClass = SchoolClassDO.builder()
                .schoolId(schoolId)
                .entryYear(entryYear)
                .schoolGradeId(schoolGrade.getId())
                .schoolYearId(schoolYearId)
                .classNo(classNo)
                .className(SchoolClassUtils.buildClassName(entryYear, gradeCatalog.getGradeName(), classNo))
                .build();
        schoolClass.clean();
        return schoolClass;
    }

    private boolean isFirstGradeClass(SchoolClassDO schoolClass, Map<Long, SchoolGradeDO> schoolGradeMap,
                                      Set<Long> firstGradeCatalogIds) {
        SchoolGradeDO schoolGrade = schoolGradeMap.get(schoolClass.getSchoolGradeId());
        return schoolGrade != null && firstGradeCatalogIds.contains(schoolGrade.getGradeCatalogId());
    }

    private SchoolYearDO buildTargetYear(Long schoolId, SchoolYearDO sourceYear, YearCatalogDO targetYearCatalog,
                                         SchoolYearClassGenerateRequest request) {
        return SchoolYearDO.builder()
                .schoolId(schoolId)
                .yearCatalogId(targetYearCatalog.getId())
                .yearStart(request.targetYearStart)
                .yearEnd(request.targetYearEnd)
                .startDate(sourceYear.getStartDate().plusYears(1))
                .endDate(sourceYear.getEndDate().plusYears(1))
                .build();
    }

    private YearCatalogDO getOrCreateYearCatalog(SchoolYearClassGenerateRequest request) {
        YearCatalogDO yearCatalog = yearCatalogMapper.selectByYearRange(request.targetYearStart, request.targetYearEnd);
        if (yearCatalog != null) {
            return yearCatalog;
        }
        yearCatalog = YearCatalogDO.builder()
                .yearStart(request.targetYearStart)
                .yearEnd(request.targetYearEnd)
                .build();
        if (request.dryRun) {
            yearCatalog.setId(buildDryRunYearCatalogId(request.targetYearStart, request.targetYearEnd));
            return yearCatalog;
        }
        yearCatalog.clean();
        yearCatalogMapper.insert(yearCatalog);
        return yearCatalog;
    }

    private SchoolYearClassGenerateRequest normalizeRequest(SchoolYearClassGenerateReqBO reqBO) {
        Integer targetYearStart = reqBO == null || reqBO.getTargetYearStart() == null
                ? LocalDate.now().getYear() : reqBO.getTargetYearStart();
        boolean dryRun = reqBO != null && Boolean.TRUE.equals(reqBO.getDryRun());
        return new SchoolYearClassGenerateRequest(targetYearStart - 1, targetYearStart,
                targetYearStart, targetYearStart + 1, dryRun);
    }

    private SchoolYearClassGenerateRespBO buildInitialResp(SchoolYearClassGenerateRequest request) {
        SchoolYearClassGenerateRespBO respBO = new SchoolYearClassGenerateRespBO();
        respBO.setDryRun(request.dryRun);
        respBO.setSourceYearStart(request.sourceYearStart);
        respBO.setSourceYearEnd(request.sourceYearEnd);
        respBO.setTargetYearStart(request.targetYearStart);
        respBO.setTargetYearEnd(request.targetYearEnd);
        return respBO;
    }

    private void increaseSkippedSchool(SchoolYearClassGenerateRespBO respBO, String reason) {
        respBO.setSkippedSchoolCount(respBO.getSkippedSchoolCount() + 1);
        increaseReason(respBO, reason);
    }

    private void increaseSkippedClass(SchoolYearClassGenerateRespBO respBO, String reason) {
        respBO.setSkippedClassCount(respBO.getSkippedClassCount() + 1);
        increaseReason(respBO, reason);
    }

    private void increaseReason(SchoolYearClassGenerateRespBO respBO, String reason) {
        respBO.getSkipReasonCounts().merge(reason, 1, Integer::sum);
    }

    private String buildClassKey(SchoolClassDO schoolClass) {
        return buildClassKey(schoolClass.getSchoolId(), schoolClass.getEntryYear(), schoolClass.getSchoolYearId(),
                schoolClass.getSchoolGradeId(), schoolClass.getClassNo());
    }

    private String buildClassKey(Long schoolId, Integer entryYear, Long schoolYearId, Long schoolGradeId,
                                 Integer classNo) {
        return schoolId + "_" + entryYear + "_" + schoolYearId + "_" + schoolGradeId + "_" + classNo;
    }

    private Long buildDryRunYearId(Long schoolId, Integer targetYearStart) {
        return -1L * (schoolId * 10000 + targetYearStart);
    }

    private Long buildDryRunYearCatalogId(Integer targetYearStart, Integer targetYearEnd) {
        return -1L * (targetYearStart * 10000L + targetYearEnd);
    }

    private record SchoolYearClassGenerateRequest(Integer sourceYearStart, Integer sourceYearEnd,
                                                   Integer targetYearStart, Integer targetYearEnd,
                                                   boolean dryRun) {
    }

}
