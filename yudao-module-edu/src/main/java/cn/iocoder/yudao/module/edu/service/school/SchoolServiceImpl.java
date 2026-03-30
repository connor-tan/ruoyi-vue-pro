package cn.iocoder.yudao.module.edu.service.school;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.GradeCatalogSimpleRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolClassSimpleRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolClassRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolClassSaveReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolGradeRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolGradeSaveReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolGradeSimpleRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolSimpleRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolSaveReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolYearRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolYearSaveReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolYearSimpleRespVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.GradeCatalogDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolClassDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolGradeDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolYearDO;
import cn.iocoder.yudao.module.edu.dal.mysql.school.GradeCatalogMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolGradeMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolClassMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolYearMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.student.StudentMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.studentclass.StudentClassMapper;
import cn.iocoder.yudao.module.system.api.ip.AreaApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.*;

/**
 * 学校信息 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class SchoolServiceImpl implements SchoolService {

    @Resource
    private SchoolMapper schoolMapper;
    @Resource
    private GradeCatalogMapper gradeCatalogMapper;
    @Resource
    private SchoolGradeMapper schoolGradeMapper;
    @Resource
    private SchoolYearMapper schoolYearMapper;
    @Resource
    private SchoolClassMapper schoolClassMapper;
    @Resource
    private StudentMapper studentMapper;
    @Resource
    private StudentClassMapper studentClassMapper;
    @Resource
    private AreaApi areaApi;

    @Override
    public Long createSchool(SchoolSaveReqVO createReqVO) {
        validateAreaSelectable(createReqVO.getAreaId());
        // 插入
        SchoolDO school = BeanUtils.toBean(createReqVO, SchoolDO.class);
        schoolMapper.insert(school);

        // 返回
        return school.getId();
    }

    @Override
    public void updateSchool(SchoolSaveReqVO updateReqVO) {
        // 校验存在
        SchoolDO school = validateSchoolExists(updateReqVO.getId());
        if (!Objects.equals(school.getAreaId(), updateReqVO.getAreaId())) {
            validateAreaSelectable(updateReqVO.getAreaId());
        }
        // 更新
        SchoolDO updateObj = BeanUtils.toBean(updateReqVO, SchoolDO.class);
        schoolMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSchool(Long id) {
        // 校验存在
        validateSchoolExists(id);
        validateSchoolUnused(id);
        // 删除
        schoolMapper.deleteById(id);

        // 删除子表
        deleteSchoolClassBySchoolId(id);
        deleteSchoolGradeBySchoolId(id);
        deleteSchoolYearBySchoolId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSchoolListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        List<Long> existedSchoolIds = convertList(schoolMapper.selectList(SchoolDO::getId, ids), SchoolDO::getId);
        if (CollUtil.isEmpty(existedSchoolIds)) {
            return;
        }
        validateSchoolUnused(existedSchoolIds);
        // 删除
        schoolMapper.deleteByIds(existedSchoolIds);

        // 删除子表
        deleteSchoolClassBySchoolIds(existedSchoolIds);
        deleteSchoolGradeBySchoolIds(existedSchoolIds);
        deleteSchoolYearBySchoolIds(existedSchoolIds);
    }


    private SchoolDO validateSchoolExists(Long id) {
        SchoolDO school = schoolMapper.selectById(id);
        if (school == null) {
            throw exception(SCHOOL_NOT_EXISTS);
        }
        return school;
    }

    private void validateAreaSelectable(Long areaId) {
        areaApi.validateAreaSelectable(Math.toIntExact(areaId));
    }

    @Override
    public SchoolDO getSchool(Long id) {
        return schoolMapper.selectById(id);
    }

    @Override
    public PageResult<SchoolDO> getSchoolPage(SchoolPageReqVO pageReqVO) {
        List<Long> areaIds = pageReqVO.getAreaId() == null ? null
                : convertList(areaApi.getSelectableAreaIds(Math.toIntExact(pageReqVO.getAreaId())), Long::valueOf);
        return schoolMapper.selectPage(pageReqVO, areaIds);
    }

    @Override
    public List<SchoolSimpleRespVO> getSchoolSimpleList() {
        return schoolMapper.selectList(new LambdaQueryWrapperX<SchoolDO>()
                        .orderByAsc(SchoolDO::getId))
                .stream()
                .map(this::buildSchoolSimpleResp)
                .collect(Collectors.toList());
    }

    // ==================== 子表（年级定义） ====================

    @Override
    public List<GradeCatalogSimpleRespVO> getGradeCatalogList() {
        List<GradeCatalogDO> gradeCatalogs = gradeCatalogMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return gradeCatalogs.stream().map(this::buildGradeCatalogResp).collect(Collectors.toList());
    }

    @Override
    public PageResult<SchoolGradeRespVO> getSchoolGradePage(PageParam pageReqVO, Long schoolId) {
        PageResult<SchoolGradeDO> pageResult = schoolGradeMapper.selectPage(pageReqVO, schoolId);
        return new PageResult<>(buildSchoolGradeRespList(pageResult.getList()), pageResult.getTotal());
    }

    @Override
    public Long createSchoolGrade(SchoolGradeSaveReqVO schoolGrade) {
        validateSchoolExists(schoolGrade.getSchoolId());
        validateGradeCatalogEnabled(schoolGrade.getGradeCatalogId());
        validateSchoolGradeUnique(null, schoolGrade.getSchoolId(), schoolGrade.getGradeCatalogId());

        SchoolGradeDO schoolGradeDO = BeanUtils.toBean(schoolGrade, SchoolGradeDO.class);
        schoolGradeDO.clean();
        schoolGradeMapper.insert(schoolGradeDO);
        return schoolGradeDO.getId();
    }

    @Override
    public void updateSchoolGrade(SchoolGradeSaveReqVO schoolGrade) {
        validateSchoolExists(schoolGrade.getSchoolId());
        validateGradeCatalogEnabled(schoolGrade.getGradeCatalogId());
        SchoolGradeDO oldSchoolGrade = validateSchoolGradeExists(schoolGrade.getId());
        validateSchoolGradeChangeable(oldSchoolGrade, schoolGrade.getGradeCatalogId());
        validateSchoolGradeUnique(schoolGrade.getId(), schoolGrade.getSchoolId(), schoolGrade.getGradeCatalogId());

        SchoolGradeDO schoolGradeDO = BeanUtils.toBean(schoolGrade, SchoolGradeDO.class);
        schoolGradeDO.clean();
        schoolGradeMapper.updateById(schoolGradeDO);
    }

    @Override
    public void deleteSchoolGrade(Long id) {
        validateSchoolGradeExists(id);
        validateSchoolGradeUnused(id);
        schoolGradeMapper.deleteById(id);
    }

    @Override
    public void deleteSchoolGradeListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        ids.forEach(this::deleteSchoolGrade);
    }

    @Override
    public SchoolGradeRespVO getSchoolGrade(Long id) {
        return buildSchoolGradeResp(validateSchoolGradeExists(id));
    }

    @Override
    public List<SchoolGradeSimpleRespVO> getSchoolGradeList(Long schoolId) {
        validateSchoolExists(schoolId);
        return buildSchoolGradeSimpleRespList(schoolGradeMapper.selectListBySchoolId(schoolId));
    }

    private SchoolGradeDO validateSchoolGradeExists(Long id) {
        SchoolGradeDO schoolGrade = schoolGradeMapper.selectById(id);
        if (schoolGrade == null) {
            throw exception(SCHOOL_GRADE_NOT_EXISTS);
        }
        return schoolGrade;
    }

    private void validateSchoolGradeUnique(Long id, Long schoolId, Long gradeCatalogId) {
        SchoolGradeDO schoolGrade = schoolGradeMapper.selectBySchoolIdAndGradeCatalogId(schoolId, gradeCatalogId);
        if (schoolGrade == null) {
            return;
        }
        if (id != null && Objects.equals(schoolGrade.getId(), id)) {
            return;
        }
        throw exception(SCHOOL_GRADE_DUPLICATE);
    }

    private void validateSchoolGradeUnused(Long schoolGradeId) {
        if (schoolClassMapper.countBySchoolGradeId(schoolGradeId) > 0) {
            throw exception(SCHOOL_GRADE_IN_USE);
        }
    }

    private void validateSchoolGradeChangeable(SchoolGradeDO schoolGrade, Long gradeCatalogId) {
        if (Objects.equals(schoolGrade.getGradeCatalogId(), gradeCatalogId)) {
            return;
        }
        if (schoolClassMapper.countBySchoolGradeId(schoolGrade.getId()) > 0) {
            throw exception(SCHOOL_GRADE_IN_USE_UPDATE);
        }
    }

    private void deleteSchoolGradeBySchoolId(Long schoolId) {
        schoolGradeMapper.deleteBySchoolId(schoolId);
    }

    private void deleteSchoolGradeBySchoolIds(List<Long> schoolIds) {
        schoolGradeMapper.deleteBySchoolIds(schoolIds);
    }

    // ==================== 子表（学年） ====================

    @Override
    public PageResult<SchoolYearRespVO> getSchoolYearPage(PageParam pageReqVO, Long schoolId) {
        PageResult<SchoolYearDO> pageResult = schoolYearMapper.selectPage(pageReqVO, schoolId);
        return new PageResult<>(BeanUtils.toBean(pageResult.getList(), SchoolYearRespVO.class), pageResult.getTotal());
    }

    @Override
    public Long createSchoolYear(SchoolYearSaveReqVO schoolYear) {
        validateSchoolExists(schoolYear.getSchoolId());
        validateSchoolYearUnique(null, schoolYear.getSchoolId(), schoolYear.getYearStart());
        SchoolYearDO schoolYearDO = BeanUtils.toBean(schoolYear, SchoolYearDO.class);
        schoolYearDO.clean(); // 清理掉创建、更新时间等相关属性值
        schoolYearMapper.insert(schoolYearDO);
        return schoolYearDO.getId();
    }

    @Override
    public void updateSchoolYear(SchoolYearSaveReqVO schoolYear) {
        validateSchoolExists(schoolYear.getSchoolId());
        // 校验存在
        validateSchoolYearExists(schoolYear.getId());
        validateSchoolYearUnique(schoolYear.getId(), schoolYear.getSchoolId(), schoolYear.getYearStart());
        // 更新
        SchoolYearDO schoolYearDO = BeanUtils.toBean(schoolYear, SchoolYearDO.class);
        schoolYearDO.clean(); // 解决更新情况下：updateTime 不更新
        schoolYearMapper.updateById(schoolYearDO);
    }

    @Override
    public void deleteSchoolYear(Long id) {
        validateSchoolYearExists(id);
        validateSchoolYearUnused(id);
        schoolYearMapper.deleteById(id);
    }

    @Override
    public void deleteSchoolYearListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        ids.forEach(this::deleteSchoolYear);
    }

    @Override
    public SchoolYearRespVO getSchoolYear(Long id) {
        return BeanUtils.toBean(schoolYearMapper.selectById(id), SchoolYearRespVO.class);
    }

    @Override
    public List<SchoolYearSimpleRespVO> getSchoolYearList(Long schoolId) {
        validateSchoolExists(schoolId);
        return schoolYearMapper.selectListBySchoolId(schoolId).stream()
                .map(this::buildSchoolYearSimpleResp)
                .collect(Collectors.toList());
    }

    private SchoolYearDO validateSchoolYearExists(Long id) {
        SchoolYearDO schoolYear = schoolYearMapper.selectById(id);
        if (schoolYear == null) {
            throw exception(SCHOOL_YEAR_NOT_EXISTS);
        }
        return schoolYear;
    }

    private void validateSchoolYearUnused(Long schoolYearId) {
        if (schoolClassMapper.countBySchoolYearId(schoolYearId) > 0) {
            throw exception(SCHOOL_YEAR_IN_USE);
        }
    }

    private void validateSchoolYearUnique(Long id, Long schoolId, Integer yearStart) {
        SchoolYearDO schoolYear = schoolYearMapper.selectBySchoolIdAndYearStart(schoolId, yearStart);
        if (schoolYear == null) {
            return;
        }
        if (id != null && Objects.equals(schoolYear.getId(), id)) {
            return;
        }
        throw exception(SCHOOL_YEAR_DUPLICATE);
    }

    private void deleteSchoolYearBySchoolId(Long schoolId) {
        schoolYearMapper.deleteBySchoolId(schoolId);
    }

    private void deleteSchoolYearBySchoolIds(List<Long> schoolIds) {
        schoolYearMapper.deleteBySchoolIds(schoolIds);
    }

    // ==================== 子表（班级） ====================

    @Override
    public PageResult<SchoolClassRespVO> getSchoolClassPage(PageParam pageReqVO, Long schoolId) {
        PageResult<SchoolClassDO> pageResult = schoolClassMapper.selectPage(pageReqVO, schoolId);
        return new PageResult<>(buildSchoolClassRespList(pageResult.getList()), pageResult.getTotal());
    }

    @Override
    public Long createSchoolClass(SchoolClassSaveReqVO schoolClass) {
        validateSchoolExists(schoolClass.getSchoolId());
        SchoolYearDO schoolYear = validateSchoolYearExists(schoolClass.getSchoolYearId());
        validateSchoolYearBelongsToSchool(schoolYear, schoolClass.getSchoolId());
        SchoolGradeDO schoolGrade = validateSchoolGradeExists(schoolClass.getSchoolGradeId());
        validateSchoolGradeBelongsToSchool(schoolGrade, schoolClass.getSchoolId());
        GradeCatalogDO gradeCatalog = validateGradeCatalogEnabled(schoolGrade.getGradeCatalogId());
        validateSchoolClassUnique(null, schoolClass);

        SchoolClassDO schoolClassDO = BeanUtils.toBean(schoolClass, SchoolClassDO.class);
        fillClassNameIfBlank(schoolClassDO, gradeCatalog);
        schoolClassDO.clean();
        schoolClassMapper.insert(schoolClassDO);
        return schoolClassDO.getId();
    }

    @Override
    public void updateSchoolClass(SchoolClassSaveReqVO schoolClass) {
        validateSchoolExists(schoolClass.getSchoolId());
        validateSchoolClassExists(schoolClass.getId());
        SchoolYearDO schoolYear = validateSchoolYearExists(schoolClass.getSchoolYearId());
        validateSchoolYearBelongsToSchool(schoolYear, schoolClass.getSchoolId());
        SchoolGradeDO schoolGrade = validateSchoolGradeExists(schoolClass.getSchoolGradeId());
        validateSchoolGradeBelongsToSchool(schoolGrade, schoolClass.getSchoolId());
        GradeCatalogDO gradeCatalog = validateGradeCatalogEnabled(schoolGrade.getGradeCatalogId());
        validateSchoolClassUnique(schoolClass.getId(), schoolClass);

        SchoolClassDO schoolClassDO = BeanUtils.toBean(schoolClass, SchoolClassDO.class);
        fillClassNameIfBlank(schoolClassDO, gradeCatalog);
        schoolClassDO.clean();
        schoolClassMapper.updateById(schoolClassDO);
    }

    @Override
    public void deleteSchoolClass(Long id) {
        validateSchoolClassExists(id);
        validateSchoolClassUnused(id);
        schoolClassMapper.deleteById(id);
    }

    @Override
    public void deleteSchoolClassListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        List<Long> existedClassIds = convertList(schoolClassMapper.selectList(SchoolClassDO::getId, ids), SchoolClassDO::getId);
        if (CollUtil.isEmpty(existedClassIds)) {
            return;
        }
        validateSchoolClassUnused(existedClassIds);
        schoolClassMapper.deleteByIds(existedClassIds);
    }

    @Override
    public SchoolClassRespVO getSchoolClass(Long id) {
        return buildSchoolClassResp(validateSchoolClassExists(id));
    }

    @Override
    public List<SchoolClassSimpleRespVO> getSchoolClassList(Long schoolId, Long schoolYearId) {
        validateSchoolExists(schoolId);
        List<SchoolClassDO> schoolClasses = schoolYearId == null
                ? schoolClassMapper.selectListBySchoolId(schoolId)
                : schoolClassMapper.selectListBySchoolIdAndSchoolYearId(schoolId, schoolYearId);
        return buildSchoolClassSimpleRespList(schoolClasses);
    }

    private SchoolClassDO validateSchoolClassExists(Long id) {
        SchoolClassDO schoolClass = schoolClassMapper.selectById(id);
        if (schoolClass == null) {
            throw exception(SCHOOL_CLASS_NOT_EXISTS);
        }
        return schoolClass;
    }

    private void validateSchoolClassUnique(Long id, SchoolClassSaveReqVO schoolClass) {
        SchoolClassDO existedClass = schoolClassMapper.selectByUniqueKey(
                schoolClass.getSchoolId(), schoolClass.getEntryYear(), schoolClass.getSchoolYearId(),
                schoolClass.getSchoolGradeId(), schoolClass.getClassNo());
        if (existedClass == null) {
            return;
        }
        if (id != null && Objects.equals(existedClass.getId(), id)) {
            return;
        }
        throw exception(SCHOOL_CLASS_DUPLICATE);
    }

    private void validateSchoolUnused(Long schoolId) {
        if (studentMapper.countByCurrentSchoolId(schoolId) > 0) {
            throw exception(SCHOOL_IN_USE_BY_STUDENT);
        }
        List<Long> classIds = convertList(schoolClassMapper.selectListBySchoolId(schoolId), SchoolClassDO::getId);
        if (CollUtil.isNotEmpty(classIds) && studentClassMapper.countByClassIds(classIds) > 0) {
            throw exception(SCHOOL_IN_USE_BY_STUDENT);
        }
    }

    private void validateSchoolUnused(List<Long> schoolIds) {
        if (studentMapper.countByCurrentSchoolIds(schoolIds) > 0) {
            throw exception(SCHOOL_IN_USE_BY_STUDENT);
        }
        List<Long> classIds = convertList(
                schoolClassMapper.selectList(SchoolClassDO::getSchoolId, schoolIds),
                SchoolClassDO::getId);
        if (CollUtil.isNotEmpty(classIds) && studentClassMapper.countByClassIds(classIds) > 0) {
            throw exception(SCHOOL_IN_USE_BY_STUDENT);
        }
    }

    private void validateSchoolClassUnused(Long classId) {
        if (studentClassMapper.countByClassId(classId) > 0) {
            throw exception(SCHOOL_CLASS_IN_USE_BY_STUDENT);
        }
    }

    private void validateSchoolClassUnused(List<Long> classIds) {
        if (studentClassMapper.countByClassIds(classIds) > 0) {
            throw exception(SCHOOL_CLASS_IN_USE_BY_STUDENT);
        }
    }

    private void deleteSchoolClassBySchoolId(Long schoolId) {
        schoolClassMapper.deleteBySchoolId(schoolId);
    }

    private void deleteSchoolClassBySchoolIds(List<Long> schoolIds) {
        schoolClassMapper.deleteBySchoolIds(schoolIds);
    }

    private GradeCatalogDO validateGradeCatalogEnabled(Long id) {
        GradeCatalogDO gradeCatalog = gradeCatalogMapper.selectById(id);
        if (gradeCatalog == null) {
            throw exception(GRADE_CATALOG_NOT_EXISTS);
        }
        if (CommonStatusEnum.isDisable(gradeCatalog.getStatus())) {
            throw exception(GRADE_CATALOG_DISABLED);
        }
        return gradeCatalog;
    }

    private void validateSchoolGradeBelongsToSchool(SchoolGradeDO schoolGrade, Long schoolId) {
        if (!Objects.equals(schoolGrade.getSchoolId(), schoolId)) {
            throw exception(SCHOOL_GRADE_NOT_BELONG_TO_SCHOOL);
        }
    }

    private void validateSchoolYearBelongsToSchool(SchoolYearDO schoolYear, Long schoolId) {
        if (!Objects.equals(schoolYear.getSchoolId(), schoolId)) {
            throw exception(SCHOOL_YEAR_NOT_BELONG_TO_SCHOOL);
        }
    }

    private GradeCatalogSimpleRespVO buildGradeCatalogResp(GradeCatalogDO gradeCatalog) {
        GradeCatalogSimpleRespVO respVO = new GradeCatalogSimpleRespVO();
        respVO.setId(gradeCatalog.getId());
        respVO.setStage(gradeCatalog.getStage());
        respVO.setGradeNo(gradeCatalog.getGradeNo());
        respVO.setGradeName(gradeCatalog.getGradeName());
        return respVO;
    }

    private SchoolSimpleRespVO buildSchoolSimpleResp(SchoolDO school) {
        SchoolSimpleRespVO respVO = new SchoolSimpleRespVO();
        respVO.setId(school.getId());
        respVO.setSchoolName(school.getSchoolName());
        return respVO;
    }

    private List<SchoolGradeRespVO> buildSchoolGradeRespList(List<SchoolGradeDO> schoolGrades) {
        Map<Long, GradeCatalogDO> gradeCatalogMap = getGradeCatalogMap(convertList(schoolGrades, SchoolGradeDO::getGradeCatalogId));
        return schoolGrades.stream()
                .map(schoolGrade -> buildSchoolGradeResp(schoolGrade, gradeCatalogMap.get(schoolGrade.getGradeCatalogId())))
                .collect(Collectors.toList());
    }

    private SchoolGradeRespVO buildSchoolGradeResp(SchoolGradeDO schoolGrade) {
        GradeCatalogDO gradeCatalog = gradeCatalogMapper.selectById(schoolGrade.getGradeCatalogId());
        return buildSchoolGradeResp(schoolGrade, gradeCatalog);
    }

    private SchoolGradeRespVO buildSchoolGradeResp(SchoolGradeDO schoolGrade, GradeCatalogDO gradeCatalog) {
        SchoolGradeRespVO respVO = new SchoolGradeRespVO();
        respVO.setId(schoolGrade.getId());
        respVO.setSchoolId(schoolGrade.getSchoolId());
        respVO.setGradeCatalogId(schoolGrade.getGradeCatalogId());
        respVO.setCreateTime(schoolGrade.getCreateTime());
        if (gradeCatalog != null) {
            respVO.setStage(gradeCatalog.getStage());
            respVO.setGradeNo(gradeCatalog.getGradeNo());
            respVO.setGradeName(gradeCatalog.getGradeName());
        }
        return respVO;
    }

    private List<SchoolGradeSimpleRespVO> buildSchoolGradeSimpleRespList(List<SchoolGradeDO> schoolGrades) {
        Map<Long, GradeCatalogDO> gradeCatalogMap = getGradeCatalogMap(convertList(schoolGrades, SchoolGradeDO::getGradeCatalogId));
        return schoolGrades.stream().map(schoolGrade -> {
            GradeCatalogDO gradeCatalog = gradeCatalogMap.get(schoolGrade.getGradeCatalogId());
            SchoolGradeSimpleRespVO respVO = new SchoolGradeSimpleRespVO();
            respVO.setId(schoolGrade.getId());
            respVO.setGradeCatalogId(schoolGrade.getGradeCatalogId());
            if (gradeCatalog != null) {
                respVO.setStage(gradeCatalog.getStage());
                respVO.setGradeNo(gradeCatalog.getGradeNo());
                respVO.setGradeName(gradeCatalog.getGradeName());
            }
            return respVO;
        }).collect(Collectors.toList());
    }

    private SchoolYearSimpleRespVO buildSchoolYearSimpleResp(SchoolYearDO schoolYear) {
        SchoolYearSimpleRespVO respVO = new SchoolYearSimpleRespVO();
        respVO.setId(schoolYear.getId());
        respVO.setYearStart(schoolYear.getYearStart());
        respVO.setYearEnd(schoolYear.getYearEnd());
        respVO.setStartDate(schoolYear.getStartDate());
        respVO.setEndDate(schoolYear.getEndDate());
        respVO.setName(buildSchoolYearName(schoolYear));
        return respVO;
    }

    private List<SchoolClassRespVO> buildSchoolClassRespList(List<SchoolClassDO> schoolClasses) {
        if (CollUtil.isEmpty(schoolClasses)) {
            return Collections.emptyList();
        }
        Map<Long, SchoolGradeDO> schoolGradeMap = schoolGradeMapper.selectList(
                        SchoolGradeDO::getId, convertList(schoolClasses, SchoolClassDO::getSchoolGradeId)).stream()
                .collect(Collectors.toMap(SchoolGradeDO::getId, Function.identity(), (item1, item2) -> item1));
        Map<Long, GradeCatalogDO> gradeCatalogMap = getGradeCatalogMap(convertList(schoolGradeMap.values(), SchoolGradeDO::getGradeCatalogId));
        Map<Long, SchoolYearDO> schoolYearMap = schoolYearMapper.selectList(
                        SchoolYearDO::getId, convertList(schoolClasses, SchoolClassDO::getSchoolYearId)).stream()
                .collect(Collectors.toMap(SchoolYearDO::getId, Function.identity(), (item1, item2) -> item1));
        return schoolClasses.stream()
                .map(schoolClass -> buildSchoolClassResp(schoolClass, schoolGradeMap, gradeCatalogMap, schoolYearMap))
                .collect(Collectors.toList());
    }

    private List<SchoolClassSimpleRespVO> buildSchoolClassSimpleRespList(List<SchoolClassDO> schoolClasses) {
        return buildSchoolClassRespList(schoolClasses).stream().map(schoolClass -> {
            SchoolClassSimpleRespVO respVO = new SchoolClassSimpleRespVO();
            respVO.setId(schoolClass.getId());
            respVO.setEntryYear(schoolClass.getEntryYear());
            respVO.setSchoolGradeId(schoolClass.getSchoolGradeId());
            respVO.setClassName(schoolClass.getClassName());
            respVO.setStage(schoolClass.getStage());
            respVO.setGradeNo(schoolClass.getGradeNo());
            respVO.setGradeName(schoolClass.getGradeName());
            respVO.setSchoolYearName(schoolClass.getSchoolYearName());
            return respVO;
        }).collect(Collectors.toList());
    }

    private SchoolClassRespVO buildSchoolClassResp(SchoolClassDO schoolClass) {
        SchoolGradeDO schoolGrade = schoolGradeMapper.selectById(schoolClass.getSchoolGradeId());
        GradeCatalogDO gradeCatalog = schoolGrade == null ? null : gradeCatalogMapper.selectById(schoolGrade.getGradeCatalogId());
        SchoolYearDO schoolYear = schoolYearMapper.selectById(schoolClass.getSchoolYearId());
        return buildSchoolClassResp(schoolClass, schoolGrade, gradeCatalog, schoolYear);
    }

    private SchoolClassRespVO buildSchoolClassResp(SchoolClassDO schoolClass, Map<Long, SchoolGradeDO> schoolGradeMap,
                                                   Map<Long, GradeCatalogDO> gradeCatalogMap,
                                                   Map<Long, SchoolYearDO> schoolYearMap) {
        SchoolGradeDO schoolGrade = schoolGradeMap.get(schoolClass.getSchoolGradeId());
        GradeCatalogDO gradeCatalog = schoolGrade == null ? null : gradeCatalogMap.get(schoolGrade.getGradeCatalogId());
        SchoolYearDO schoolYear = schoolYearMap.get(schoolClass.getSchoolYearId());
        return buildSchoolClassResp(schoolClass, schoolGrade, gradeCatalog, schoolYear);
    }

    private SchoolClassRespVO buildSchoolClassResp(SchoolClassDO schoolClass, SchoolGradeDO schoolGrade,
                                                   GradeCatalogDO gradeCatalog, SchoolYearDO schoolYear) {
        SchoolClassRespVO respVO = new SchoolClassRespVO();
        respVO.setId(schoolClass.getId());
        respVO.setSchoolId(schoolClass.getSchoolId());
        respVO.setEntryYear(schoolClass.getEntryYear());
        respVO.setSchoolGradeId(schoolClass.getSchoolGradeId());
        respVO.setSchoolYearId(schoolClass.getSchoolYearId());
        respVO.setClassNo(schoolClass.getClassNo());
        respVO.setClassName(schoolClass.getClassName());
        respVO.setCreateTime(schoolClass.getCreateTime());
        if (schoolYear != null) {
            respVO.setSchoolYearName(buildSchoolYearName(schoolYear));
        }
        if (gradeCatalog != null) {
            respVO.setStage(gradeCatalog.getStage());
            respVO.setGradeNo(gradeCatalog.getGradeNo());
            respVO.setGradeName(gradeCatalog.getGradeName());
        }
        return respVO;
    }

    private Map<Long, GradeCatalogDO> getGradeCatalogMap(Collection<Long> gradeCatalogIds) {
        if (CollUtil.isEmpty(gradeCatalogIds)) {
            return Collections.emptyMap();
        }
        return gradeCatalogMapper.selectList(GradeCatalogDO::getId, gradeCatalogIds).stream()
                .collect(Collectors.toMap(GradeCatalogDO::getId, Function.identity(), (item1, item2) -> item1));
    }

    private String buildSchoolYearName(SchoolYearDO schoolYear) {
        return schoolYear.getYearStart() + "-" + schoolYear.getYearEnd() + "学年";
    }

    private void fillClassNameIfBlank(SchoolClassDO schoolClass, GradeCatalogDO gradeCatalog) {
        if (StrUtil.isNotBlank(schoolClass.getClassName())) {
            return;
        }
        String gradeName = gradeCatalog != null ? gradeCatalog.getGradeName() : "";
        schoolClass.setClassName(SchoolClassUtils.buildClassName(schoolClass.getEntryYear(), gradeName, schoolClass.getClassNo()));
    }

}
