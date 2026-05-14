package cn.iocoder.yudao.module.edu.service.school;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.controller.app.school.vo.AppSchoolClassSimpleRespVO;
import cn.iocoder.yudao.module.edu.controller.app.school.vo.AppSchoolGradeSimpleRespVO;
import cn.iocoder.yudao.module.edu.controller.app.school.vo.AppSchoolSimpleRespVO;
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
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolStageDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolYearDO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.YearCatalogDO;
import cn.iocoder.yudao.module.edu.dal.mysql.school.GradeCatalogMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolGradeMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolClassMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolStageMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolYearMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.school.YearCatalogMapper;
import cn.iocoder.yudao.module.edu.dal.dataobject.station.StationDO;
import cn.iocoder.yudao.module.edu.dal.mysql.student.StudentMapper;
import cn.iocoder.yudao.module.edu.dal.mysql.studentclass.StudentClassMapper;
import cn.iocoder.yudao.module.edu.service.station.StationService;
import cn.iocoder.yudao.module.repo.dal.dataobject.warehouse.RepoWarehouseDO;
import cn.iocoder.yudao.module.repo.service.warehouse.RepoWarehouseService;
import cn.iocoder.yudao.module.system.api.ip.AreaApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    private static final Map<String, String> STAGE_NAME_MAP = Map.of(
            "kindergarten", "幼儿园",
            "primary", "小学",
            "middle", "初中");

    @Resource
    private SchoolMapper schoolMapper;
    @Resource
    private SchoolStageMapper schoolStageMapper;
    @Resource
    private GradeCatalogMapper gradeCatalogMapper;
    @Resource
    private SchoolGradeMapper schoolGradeMapper;
    @Resource
    private SchoolYearMapper schoolYearMapper;
    @Resource
    private YearCatalogMapper yearCatalogMapper;
    @Resource
    private SchoolClassMapper schoolClassMapper;
    @Resource
    private StudentMapper studentMapper;
    @Resource
    private StudentClassMapper studentClassMapper;
    @Resource
    private AreaApi areaApi;
    @Resource
    private StationService stationService;
    @Resource
    private RepoWarehouseService warehouseService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSchool(SchoolSaveReqVO createReqVO) {
        validateAreaSelectable(createReqVO.getAreaId());
        stationService.validateStationBindable(createReqVO.getStationId());
        warehouseService.validateWarehouseBindable(createReqVO.getWarehouseId());
        List<String> stageCodes = normalizeStageCodes(createReqVO.getStageCodes());
        // 插入
        SchoolDO school = BeanUtils.toBean(createReqVO, SchoolDO.class);
        schoolMapper.insert(school);
        saveSchoolStages(school.getId(), stageCodes);

        // 返回
        return school.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSchool(SchoolSaveReqVO updateReqVO) {
        // 校验存在
        SchoolDO school = validateSchoolExists(updateReqVO.getId());
        List<String> stageCodes = normalizeStageCodes(updateReqVO.getStageCodes());
        validateSchoolStageChangeable(updateReqVO.getId(), stageCodes);
        if (!Objects.equals(school.getAreaId(), updateReqVO.getAreaId())) {
            validateAreaSelectable(updateReqVO.getAreaId());
        }
        stationService.validateStationBindable(updateReqVO.getStationId());
        warehouseService.validateWarehouseBindable(updateReqVO.getWarehouseId());
        // 更新
        SchoolDO updateObj = BeanUtils.toBean(updateReqVO, SchoolDO.class);
        schoolMapper.updateById(updateObj);
        saveSchoolStages(updateReqVO.getId(), stageCodes);
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
        schoolStageMapper.deleteBySchoolId(id);
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
        schoolStageMapper.deleteBySchoolIds(existedSchoolIds);
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
        SchoolDO school = schoolMapper.selectById(id);
        fillSchoolStages(school);
        fillSchoolStations(school);
        fillSchoolWarehouses(school);
        return school;
    }

    @Override
    public PageResult<SchoolDO> getSchoolPage(SchoolPageReqVO pageReqVO) {
        List<Long> areaIds = pageReqVO.getAreaId() == null ? null
                : convertList(areaApi.getSelectableAreaIds(Math.toIntExact(pageReqVO.getAreaId())), Long::valueOf);
        List<Long> stageSchoolIds = StrUtil.isBlank(pageReqVO.getStageCode()) ? null
                : schoolStageMapper.selectSchoolIdsByStage(pageReqVO.getStageCode());
        if (StrUtil.isNotBlank(pageReqVO.getStageCode()) && CollUtil.isEmpty(stageSchoolIds)) {
            return PageResult.empty();
        }
        PageResult<SchoolDO> pageResult = schoolMapper.selectPage(pageReqVO, areaIds, stageSchoolIds);
        fillSchoolStages(pageResult.getList());
        fillSchoolStations(pageResult.getList());
        fillSchoolWarehouses(pageResult.getList());
        return pageResult;
    }

    @Override
    public List<SchoolSimpleRespVO> getSchoolSimpleList() {
        List<SchoolDO> schools = schoolMapper.selectList(new LambdaQueryWrapperX<SchoolDO>()
                .orderByAsc(SchoolDO::getId));
        fillSchoolStages(schools);
        fillSchoolStations(schools);
        fillSchoolWarehouses(schools);
        return schools.stream()
                .map(this::buildSchoolSimpleResp)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppSchoolSimpleRespVO> getAppSchoolSimpleList(Long areaId) {
        List<SchoolDO> schools;
        if (areaId == null) {
            schools = schoolMapper.selectList(new LambdaQueryWrapperX<SchoolDO>().orderByAsc(SchoolDO::getId));
        } else {
            List<Long> areaIds = convertList(areaApi.getSelectableAreaIds(Math.toIntExact(areaId)), Long::valueOf);
            if (CollUtil.isEmpty(areaIds)) {
                return Collections.emptyList();
            }
            schools = schoolMapper.selectListByAreaIds(areaIds);
        }
        return schools.stream().map(this::buildAppSchoolSimpleResp).collect(Collectors.toList());
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
        GradeCatalogDO gradeCatalog = validateGradeCatalogEnabled(schoolGrade.getGradeCatalogId());
        validateSchoolGradeStageAllowed(schoolGrade.getSchoolId(), gradeCatalog);
        validateSchoolGradeUnique(null, schoolGrade.getSchoolId(), schoolGrade.getGradeCatalogId());

        SchoolGradeDO schoolGradeDO = BeanUtils.toBean(schoolGrade, SchoolGradeDO.class);
        schoolGradeDO.clean();
        schoolGradeMapper.insert(schoolGradeDO);
        return schoolGradeDO.getId();
    }

    @Override
    public void updateSchoolGrade(SchoolGradeSaveReqVO schoolGrade) {
        validateSchoolExists(schoolGrade.getSchoolId());
        GradeCatalogDO gradeCatalog = validateGradeCatalogEnabled(schoolGrade.getGradeCatalogId());
        validateSchoolGradeStageAllowed(schoolGrade.getSchoolId(), gradeCatalog);
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
        schoolGradeMapper.deletePhysicallyById(id);
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

    @Override
    public List<AppSchoolGradeSimpleRespVO> getAppSchoolGradeSimpleList(Long schoolId) {
        validateSchoolExists(schoolId);
        return buildAppSchoolGradeSimpleRespList(schoolGradeMapper.selectListBySchoolId(schoolId));
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
        schoolGradeMapper.deletePhysicallyBySchoolId(schoolId);
    }

    private void deleteSchoolGradeBySchoolIds(List<Long> schoolIds) {
        schoolGradeMapper.deletePhysicallyBySchoolIds(schoolIds);
    }

    // ==================== 子表（学年） ====================

    @Override
    public PageResult<SchoolYearRespVO> getSchoolYearPage(PageParam pageReqVO, Long schoolId) {
        PageResult<SchoolYearDO> pageResult = schoolYearMapper.selectPage(pageReqVO, schoolId);
        return new PageResult<>(buildSchoolYearRespList(pageResult.getList()), pageResult.getTotal());
    }

    @Override
    public Long createSchoolYear(SchoolYearSaveReqVO schoolYear) {
        validateSchoolExists(schoolYear.getSchoolId());
        YearCatalogDO yearCatalog = validateYearCatalogExists(schoolYear.getYearCatalogId());
        validateSchoolYearUnique(null, schoolYear.getSchoolId(), yearCatalog.getId());
        SchoolYearDO schoolYearDO = BeanUtils.toBean(schoolYear, SchoolYearDO.class);
        applyYearCatalog(schoolYearDO, yearCatalog);
        schoolYearDO.clean(); // 清理掉创建、更新时间等相关属性值
        schoolYearMapper.insert(schoolYearDO);
        return schoolYearDO.getId();
    }

    @Override
    public void updateSchoolYear(SchoolYearSaveReqVO schoolYear) {
        validateSchoolExists(schoolYear.getSchoolId());
        SchoolYearDO oldSchoolYear = validateSchoolYearExists(schoolYear.getId());
        validateSchoolYearBelongsToSchool(oldSchoolYear, schoolYear.getSchoolId());
        YearCatalogDO yearCatalog = validateYearCatalogExists(schoolYear.getYearCatalogId());
        validateSchoolYearUnique(schoolYear.getId(), schoolYear.getSchoolId(), yearCatalog.getId());
        validateSchoolYearChangeable(oldSchoolYear, yearCatalog.getId());
        SchoolYearDO schoolYearDO = BeanUtils.toBean(schoolYear, SchoolYearDO.class);
        applyYearCatalog(schoolYearDO, yearCatalog);
        schoolYearDO.clean(); // 解决更新情况下：updateTime 不更新
        schoolYearMapper.updateById(schoolYearDO);
    }

    @Override
    public void deleteSchoolYear(Long id) {
        validateSchoolYearExists(id);
        validateSchoolYearUnused(id);
        schoolYearMapper.deletePhysicallyById(id);
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
        SchoolYearDO schoolYear = schoolYearMapper.selectById(id);
        return schoolYear == null ? null : buildSchoolYearResp(schoolYear);
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

    private void validateSchoolYearUnique(Long id, Long schoolId, Long yearCatalogId) {
        SchoolYearDO schoolYear = schoolYearMapper.selectBySchoolIdAndYearCatalogId(schoolId, yearCatalogId);
        if (schoolYear == null) {
            return;
        }
        if (id != null && Objects.equals(schoolYear.getId(), id)) {
            return;
        }
        throw exception(SCHOOL_YEAR_DUPLICATE);
    }

    private void validateSchoolYearChangeable(SchoolYearDO schoolYear, Long yearCatalogId) {
        if (Objects.equals(schoolYear.getYearCatalogId(), yearCatalogId)) {
            return;
        }
        if (schoolClassMapper.countBySchoolYearId(schoolYear.getId()) > 0) {
            throw exception(SCHOOL_YEAR_IN_USE_UPDATE);
        }
    }

    private void deleteSchoolYearBySchoolId(Long schoolId) {
        schoolYearMapper.deletePhysicallyBySchoolId(schoolId);
    }

    private void deleteSchoolYearBySchoolIds(List<Long> schoolIds) {
        schoolYearMapper.deletePhysicallyBySchoolIds(schoolIds);
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
        schoolClassMapper.deletePhysicallyById(id);
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
        schoolClassMapper.deletePhysicallyByIds(existedClassIds);
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

    @Override
    public List<AppSchoolClassSimpleRespVO> getAppCurrentSchoolClassSimpleList(Long schoolId, Long schoolGradeId) {
        validateSchoolExists(schoolId);
        SchoolGradeDO schoolGrade = validateSchoolGradeExists(schoolGradeId);
        validateSchoolGradeBelongsToSchool(schoolGrade, schoolId);
        SchoolYearDO currentSchoolYear = schoolYearMapper.selectCurrentBySchoolId(schoolId, LocalDate.now());
        if (currentSchoolYear == null) {
            return Collections.emptyList();
        }
        return schoolClassMapper.selectListBySchoolIdAndSchoolYearIdAndSchoolGradeId(
                        schoolId, currentSchoolYear.getId(), schoolGradeId)
                .stream()
                .map(this::buildAppSchoolClassSimpleResp)
                .collect(Collectors.toList());
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

    private List<String> normalizeStageCodes(List<String> stageCodes) {
        if (CollUtil.isEmpty(stageCodes)) {
            throw exception(SCHOOL_STAGE_REQUIRED);
        }
        List<String> normalizedStageCodes = stageCodes.stream()
                .filter(StrUtil::isNotBlank)
                .map(StrUtil::trim)
                .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), ArrayList::new));
        if (CollUtil.isEmpty(normalizedStageCodes)) {
            throw exception(SCHOOL_STAGE_REQUIRED);
        }
        Set<String> enabledStageCodes = getEnabledStageCodes();
        normalizedStageCodes.forEach(stageCode -> {
            if (!enabledStageCodes.contains(stageCode)) {
                throw exception(SCHOOL_STAGE_INVALID, stageCode);
            }
        });
        return normalizedStageCodes;
    }

    private Set<String> getEnabledStageCodes() {
        return gradeCatalogMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus()).stream()
                .map(GradeCatalogDO::getStage)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void validateSchoolStageChangeable(Long schoolId, Collection<String> targetStageCodes) {
        List<SchoolGradeDO> schoolGrades = schoolGradeMapper.selectListBySchoolId(schoolId);
        if (CollUtil.isEmpty(schoolGrades)) {
            return;
        }
        Set<String> targetStageSet = new LinkedHashSet<>(targetStageCodes);
        Set<String> usedStages = getGradeCatalogMap(convertList(schoolGrades, SchoolGradeDO::getGradeCatalogId)).values().stream()
                .map(GradeCatalogDO::getStage)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        usedStages.removeAll(targetStageSet);
        if (CollUtil.isNotEmpty(usedStages)) {
            throw exception(SCHOOL_STAGE_IN_USE, buildStageNames(usedStages));
        }
    }

    private void validateSchoolGradeStageAllowed(Long schoolId, GradeCatalogDO gradeCatalog) {
        List<String> schoolStageCodes = getSchoolStageCodes(schoolId);
        if (CollUtil.isEmpty(schoolStageCodes)) {
            throw exception(SCHOOL_STAGE_NOT_CONFIGURED);
        }
        if (!schoolStageCodes.contains(gradeCatalog.getStage())) {
            throw exception(SCHOOL_GRADE_STAGE_NOT_ALLOWED, getStageName(gradeCatalog.getStage()), gradeCatalog.getGradeName());
        }
    }

    private void saveSchoolStages(Long schoolId, List<String> stageCodes) {
        schoolStageMapper.deleteBySchoolId(schoolId);
        List<SchoolStageDO> schoolStages = stageCodes.stream().map(stageCode -> {
            SchoolStageDO schoolStage = SchoolStageDO.builder()
                    .schoolId(schoolId)
                    .stage(stageCode)
                    .build();
            schoolStage.clean();
            return schoolStage;
        }).collect(Collectors.toList());
        schoolStageMapper.insertBatch(schoolStages);
    }

    private List<String> getSchoolStageCodes(Long schoolId) {
        return schoolStageMapper.selectListBySchoolId(schoolId).stream()
                .map(SchoolStageDO::getStage)
                .collect(Collectors.toList());
    }

    private void fillSchoolStages(SchoolDO school) {
        if (school == null) {
            return;
        }
        school.setStageCodes(getSchoolStageCodes(school.getId()));
    }

    private void fillSchoolStages(List<SchoolDO> schools) {
        if (CollUtil.isEmpty(schools)) {
            return;
        }
        Map<Long, List<String>> stageCodeMap = schoolStageMapper.selectListBySchoolIds(convertList(schools, SchoolDO::getId)).stream()
                .collect(Collectors.groupingBy(SchoolStageDO::getSchoolId,
                        Collectors.mapping(SchoolStageDO::getStage, Collectors.toList())));
        schools.forEach(school -> school.setStageCodes(stageCodeMap.getOrDefault(school.getId(), Collections.emptyList())));
    }

    private void fillSchoolStations(SchoolDO school) {
        if (school == null) {
            return;
        }
        fillSchoolStations(List.of(school));
    }

    private void fillSchoolStations(List<SchoolDO> schools) {
        if (CollUtil.isEmpty(schools)) {
            return;
        }
        Map<Long, StationDO> stationMap = stationService.getStationMap(
                schools.stream().map(SchoolDO::getStationId).filter(Objects::nonNull).toList());
        schools.forEach(school -> {
            StationDO station = stationMap.get(school.getStationId());
            school.setStationName(station == null ? null : station.getStationName());
            school.setStationAreaId(station == null ? null : station.getAreaId());
            school.setStationAreaName(station == null || station.getAreaId() == null
                    ? null : AreaUtils.format(station.getAreaId().intValue()));
        });
    }

    private void fillSchoolWarehouses(SchoolDO school) {
        if (school == null) {
            return;
        }
        fillSchoolWarehouses(List.of(school));
    }

    private void fillSchoolWarehouses(List<SchoolDO> schools) {
        if (CollUtil.isEmpty(schools)) {
            return;
        }
        Map<Long, RepoWarehouseDO> warehouseMap = warehouseService.getWarehouseMap(
                schools.stream().map(SchoolDO::getWarehouseId).filter(Objects::nonNull).toList());
        schools.forEach(school -> {
            RepoWarehouseDO warehouse = warehouseMap.get(school.getWarehouseId());
            school.setWarehouseName(warehouse == null ? null : warehouse.getName());
        });
    }

    private String buildStageNames(Collection<String> stageCodes) {
        return stageCodes.stream().map(this::getStageName).collect(Collectors.joining("、"));
    }

    private String getStageName(String stageCode) {
        return STAGE_NAME_MAP.getOrDefault(stageCode, stageCode);
    }

    private void deleteSchoolClassBySchoolId(Long schoolId) {
        schoolClassMapper.deletePhysicallyBySchoolId(schoolId);
    }

    private void deleteSchoolClassBySchoolIds(List<Long> schoolIds) {
        schoolClassMapper.deletePhysicallyBySchoolIds(schoolIds);
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
        respVO.setAliasName(gradeCatalog.getAliasName());
        return respVO;
    }

    private SchoolSimpleRespVO buildSchoolSimpleResp(SchoolDO school) {
        SchoolSimpleRespVO respVO = new SchoolSimpleRespVO();
        respVO.setId(school.getId());
        respVO.setSchoolName(school.getSchoolName());
        respVO.setStationId(school.getStationId());
        respVO.setStationName(school.getStationName());
        respVO.setStationAreaId(school.getStationAreaId());
        respVO.setStationAreaName(school.getStationAreaName());
        respVO.setWarehouseId(school.getWarehouseId());
        respVO.setWarehouseName(school.getWarehouseName());
        respVO.setStageCodes(school.getStageCodes());
        return respVO;
    }

    private AppSchoolSimpleRespVO buildAppSchoolSimpleResp(SchoolDO school) {
        AppSchoolSimpleRespVO respVO = new AppSchoolSimpleRespVO();
        respVO.setId(school.getId());
        respVO.setSchoolName(school.getSchoolName());
        respVO.setAreaId(school.getAreaId());
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
            respVO.setAliasName(gradeCatalog.getAliasName());
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
                respVO.setAliasName(gradeCatalog.getAliasName());
            }
            return respVO;
        }).collect(Collectors.toList());
    }

    private List<AppSchoolGradeSimpleRespVO> buildAppSchoolGradeSimpleRespList(List<SchoolGradeDO> schoolGrades) {
        Map<Long, GradeCatalogDO> gradeCatalogMap = getGradeCatalogMap(
                convertList(schoolGrades, SchoolGradeDO::getGradeCatalogId));
        return schoolGrades.stream().map(schoolGrade -> {
            GradeCatalogDO gradeCatalog = gradeCatalogMap.get(schoolGrade.getGradeCatalogId());
            AppSchoolGradeSimpleRespVO respVO = new AppSchoolGradeSimpleRespVO();
            respVO.setId(schoolGrade.getId());
            respVO.setGradeCatalogId(schoolGrade.getGradeCatalogId());
            if (gradeCatalog != null) {
                respVO.setStage(gradeCatalog.getStage());
                respVO.setGradeNo(gradeCatalog.getGradeNo());
                respVO.setGradeName(gradeCatalog.getGradeName());
                respVO.setAliasName(gradeCatalog.getAliasName());
            }
            return respVO;
        }).collect(Collectors.toList());
    }

    private SchoolYearSimpleRespVO buildSchoolYearSimpleResp(SchoolYearDO schoolYear) {
        SchoolYearSimpleRespVO respVO = new SchoolYearSimpleRespVO();
        respVO.setId(schoolYear.getId());
        respVO.setYearCatalogId(schoolYear.getYearCatalogId());
        respVO.setYearStart(schoolYear.getYearStart());
        respVO.setYearEnd(schoolYear.getYearEnd());
        respVO.setStartDate(schoolYear.getStartDate());
        respVO.setEndDate(schoolYear.getEndDate());
        respVO.setName(buildSchoolYearName(schoolYear));
        return respVO;
    }

    private List<SchoolYearRespVO> buildSchoolYearRespList(List<SchoolYearDO> schoolYears) {
        return schoolYears.stream().map(this::buildSchoolYearResp).collect(Collectors.toList());
    }

    private SchoolYearRespVO buildSchoolYearResp(SchoolYearDO schoolYear) {
        SchoolYearRespVO respVO = BeanUtils.toBean(schoolYear, SchoolYearRespVO.class);
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
            respVO.setAliasName(schoolClass.getAliasName());
            respVO.setSchoolYearName(schoolClass.getSchoolYearName());
            return respVO;
        }).collect(Collectors.toList());
    }

    private AppSchoolClassSimpleRespVO buildAppSchoolClassSimpleResp(SchoolClassDO schoolClass) {
        AppSchoolClassSimpleRespVO respVO = new AppSchoolClassSimpleRespVO();
        respVO.setId(schoolClass.getId());
        respVO.setSchoolGradeId(schoolClass.getSchoolGradeId());
        respVO.setSchoolYearId(schoolClass.getSchoolYearId());
        respVO.setEntryYear(schoolClass.getEntryYear());
        respVO.setClassName(schoolClass.getClassName());
        return respVO;
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
            respVO.setAliasName(gradeCatalog.getAliasName());
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

    private YearCatalogDO validateYearCatalogExists(Long yearCatalogId) {
        YearCatalogDO yearCatalog = yearCatalogMapper.selectById(yearCatalogId);
        if (yearCatalog == null) {
            throw exception(YEAR_CATALOG_NOT_EXISTS);
        }
        return yearCatalog;
    }

    private void applyYearCatalog(SchoolYearDO schoolYear, YearCatalogDO yearCatalog) {
        schoolYear.setYearCatalogId(yearCatalog.getId());
        schoolYear.setYearStart(yearCatalog.getYearStart());
        schoolYear.setYearEnd(yearCatalog.getYearEnd());
    }

    private void fillClassNameIfBlank(SchoolClassDO schoolClass, GradeCatalogDO gradeCatalog) {
        if (StrUtil.isNotBlank(schoolClass.getClassName())) {
            return;
        }
        String gradeName = gradeCatalog != null ? gradeCatalog.getGradeName() : "";
        schoolClass.setClassName(SchoolClassUtils.buildClassName(schoolClass.getEntryYear(), gradeName, schoolClass.getClassNo()));
    }

}
