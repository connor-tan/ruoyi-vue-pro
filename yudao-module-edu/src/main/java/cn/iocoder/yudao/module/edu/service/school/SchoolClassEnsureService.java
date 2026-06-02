package cn.iocoder.yudao.module.edu.service.school;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.edu.controller.app.school.vo.AppSchoolClassSimpleRespVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.GradeCatalogDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolClassDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolGradeDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolYearDO;
import cn.iocoder.yudao.module.edu.dal.mysql.school.GradeCatalogMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolClassMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolGradeMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolYearMapper;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.*;

/**
 * 学校班级按需供给服务。
 */
@Service
@Validated
public class SchoolClassEnsureService {

    @Resource
    private SchoolYearMapper schoolYearMapper;
    @Resource
    private SchoolGradeMapper schoolGradeMapper;
    @Resource
    private GradeCatalogMapper gradeCatalogMapper;
    @Resource
    private SchoolClassMapper schoolClassMapper;

    public SchoolClassDO ensureSchoolClass(Long schoolId, Long schoolYearId, Long schoolGradeId, Integer classNo) {
        SchoolYearDO schoolYear = validateSchoolYear(schoolId, schoolYearId);
        SchoolGradeDO schoolGrade = validateSchoolGrade(schoolId, schoolGradeId);
        GradeCatalogDO gradeCatalog = validateGradeCatalog(schoolGrade.getGradeCatalogId());
        validateClassNoWithinCapacity(schoolGrade, classNo);
        Integer entryYear = resolveEntryYear(schoolYear, gradeCatalog);

        SchoolClassDO existedClass = schoolClassMapper.selectByUniqueKey(schoolId, entryYear, schoolYearId,
                schoolGradeId, classNo);
        if (existedClass != null) {
            return existedClass;
        }

        SchoolClassDO schoolClass = SchoolClassDO.builder()
                .schoolId(schoolId)
                .entryYear(entryYear)
                .schoolYearId(schoolYearId)
                .schoolGradeId(schoolGradeId)
                .classNo(classNo)
                .className(SchoolClassUtils.buildClassName(entryYear, gradeCatalog.getGradeName(), classNo))
                .build();
        schoolClass.clean();
        try {
            schoolClassMapper.insert(schoolClass);
            return schoolClass;
        } catch (DuplicateKeyException ex) {
            SchoolClassDO conflictClass = schoolClassMapper.selectByUniqueKey(schoolId, entryYear, schoolYearId,
                    schoolGradeId, classNo);
            if (conflictClass != null) {
                return conflictClass;
            }
            throw ex;
        }
    }

    public List<AppSchoolClassSimpleRespVO> buildClassOptions(Long schoolId, Long schoolYearId, Long schoolGradeId) {
        SchoolYearDO schoolYear = validateSchoolYear(schoolId, schoolYearId);
        SchoolGradeDO schoolGrade = validateSchoolGrade(schoolId, schoolGradeId);
        GradeCatalogDO gradeCatalog = validateGradeCatalog(schoolGrade.getGradeCatalogId());
        Integer maxClassNo = schoolGrade.getMaxClassNo() == null ? 0 : schoolGrade.getMaxClassNo();
        if (maxClassNo <= 0) {
            return List.of();
        }
        Integer entryYear = resolveEntryYear(schoolYear, gradeCatalog);
        Map<Integer, SchoolClassDO> existedClassMap = schoolClassMapper
                .selectListBySchoolIdAndSchoolYearIdAndSchoolGradeId(schoolId, schoolYearId, schoolGradeId).stream()
                .filter(item -> item.getClassNo() != null)
                .collect(Collectors.toMap(SchoolClassDO::getClassNo, Function.identity(), (item1, item2) -> item1));
        return IntStream.rangeClosed(1, maxClassNo)
                .mapToObj(classNo -> createClassOption(schoolYear, schoolGrade, gradeCatalog, entryYear, classNo,
                        existedClassMap.get(classNo)))
                .collect(Collectors.toList());
    }

    public Integer resolveEntryYear(SchoolYearDO schoolYear, GradeCatalogDO gradeCatalog) {
        List<GradeCatalogDO> enabledGradeCatalogs = gradeCatalogMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
        int gradeOffset = SchoolGradeSequenceUtils.resolveGradeOffset(gradeCatalog, enabledGradeCatalogs);
        return schoolYear.getYearStart() - gradeOffset;
    }

    public void validateClassNoWithinCapacity(SchoolGradeDO schoolGrade, Integer classNo) {
        Integer maxClassNo = schoolGrade.getMaxClassNo() == null ? 0 : schoolGrade.getMaxClassNo();
        if (maxClassNo <= 0) {
            throw exception(SCHOOL_GRADE_CLASS_CAPACITY_NOT_CONFIGURED);
        }
        if (classNo == null || classNo < 1 || classNo > maxClassNo) {
            throw exception(SCHOOL_CLASS_NO_EXCEEDS_CAPACITY);
        }
    }

    private SchoolYearDO validateSchoolYear(Long schoolId, Long schoolYearId) {
        SchoolYearDO schoolYear = schoolYearMapper.selectById(schoolYearId);
        if (schoolYear == null) {
            throw exception(SCHOOL_YEAR_NOT_EXISTS);
        }
        if (!schoolId.equals(schoolYear.getSchoolId())) {
            throw exception(SCHOOL_YEAR_NOT_BELONG_TO_SCHOOL);
        }
        return schoolYear;
    }

    private SchoolGradeDO validateSchoolGrade(Long schoolId, Long schoolGradeId) {
        SchoolGradeDO schoolGrade = schoolGradeMapper.selectById(schoolGradeId);
        if (schoolGrade == null) {
            throw exception(SCHOOL_GRADE_NOT_EXISTS);
        }
        if (!schoolId.equals(schoolGrade.getSchoolId())) {
            throw exception(SCHOOL_GRADE_NOT_BELONG_TO_SCHOOL);
        }
        return schoolGrade;
    }

    private GradeCatalogDO validateGradeCatalog(Long gradeCatalogId) {
        GradeCatalogDO gradeCatalog = gradeCatalogMapper.selectById(gradeCatalogId);
        if (gradeCatalog == null) {
            throw exception(GRADE_CATALOG_NOT_EXISTS);
        }
        return gradeCatalog;
    }

    private AppSchoolClassSimpleRespVO createClassOption(SchoolYearDO schoolYear, SchoolGradeDO schoolGrade,
                                                        GradeCatalogDO gradeCatalog, Integer entryYear,
                                                        Integer classNo, SchoolClassDO existedClass) {
        AppSchoolClassSimpleRespVO respVO = new AppSchoolClassSimpleRespVO();
        respVO.setId(existedClass == null ? null : existedClass.getId());
        respVO.setOptionKey("classNo:" + classNo);
        respVO.setSchoolGradeId(schoolGrade.getId());
        respVO.setSchoolYearId(schoolYear.getId());
        respVO.setEntryYear(entryYear);
        respVO.setClassNo(classNo);
        respVO.setClassName(existedClass == null ? SchoolClassUtils.buildClassName(entryYear,
                gradeCatalog.getGradeName(), classNo) : existedClass.getClassName());
        respVO.setExists(existedClass != null);
        return respVO;
    }

}
