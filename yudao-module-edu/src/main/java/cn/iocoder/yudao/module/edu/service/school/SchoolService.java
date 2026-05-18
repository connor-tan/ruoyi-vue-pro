package cn.iocoder.yudao.module.edu.service.school;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.edu.controller.app.school.vo.AppSchoolClassSimpleRespVO;
import cn.iocoder.yudao.module.edu.controller.app.school.vo.AppSchoolGradeSimpleRespVO;
import cn.iocoder.yudao.module.edu.controller.app.school.vo.AppSchoolSimpleRespVO;
import cn.iocoder.yudao.module.edu.controller.app.school.vo.AppSchoolYearSimpleRespVO;
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
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 学校信息 Service 接口
 *
 * @author 芋道源码
 */
public interface SchoolService {

    /**
     * 创建学校信息
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSchool(@Valid SchoolSaveReqVO createReqVO);

    /**
     * 更新学校信息
     *
     * @param updateReqVO 更新信息
     */
    void updateSchool(@Valid SchoolSaveReqVO updateReqVO);

    /**
     * 删除学校信息
     *
     * @param id 编号
     */
    void deleteSchool(Long id);

    /**
     * 批量删除学校信息
     *
     * @param ids 编号
     */
    void deleteSchoolListByIds(List<Long> ids);

    /**
     * 获得学校信息
     *
     * @param id 编号
     * @return 学校信息
     */
    SchoolDO getSchool(Long id);

    /**
     * 获得学校信息分页
     *
     * @param pageReqVO 分页查询
     * @return 学校信息分页
     */
    PageResult<SchoolDO> getSchoolPage(SchoolPageReqVO pageReqVO);

    /**
     * 获得学校精简列表
     *
     * @return 学校精简列表
     */
    List<SchoolSimpleRespVO> getSchoolSimpleList();

    /**
     * 获得 App 学校精简列表。
     *
     * @param areaId 地区编号
     * @return 学校精简列表
     */
    List<AppSchoolSimpleRespVO> getAppSchoolSimpleList(Long areaId);

    // ==================== 子表（年级定义） ====================

    /**
     * 获得年级目录列表
     *
     * @return 年级目录列表
     */
    List<GradeCatalogSimpleRespVO> getGradeCatalogList();

    /**
     * 获得年级定义分页
     *
     * @param pageReqVO 分页查询
     * @param schoolId 学校ID
     * @return 年级定义分页
     */
    PageResult<SchoolGradeRespVO> getSchoolGradePage(PageParam pageReqVO, Long schoolId);

    /**
     * 创建年级定义
     *
     * @param schoolGrade 创建信息
     * @return 编号
     */
    Long createSchoolGrade(@Valid SchoolGradeSaveReqVO schoolGrade);

    /**
     * 更新年级定义
     *
     * @param schoolGrade 更新信息
     */
    void updateSchoolGrade(@Valid SchoolGradeSaveReqVO schoolGrade);

    /**
     * 删除年级定义
     *
     * @param id 编号
     */
    void deleteSchoolGrade(Long id);

    /**
     * 批量删除年级定义
     *
     * @param ids 编号
     */
    void deleteSchoolGradeListByIds(List<Long> ids);

    /**
     * 获得年级定义
     *
     * @param id 编号
     * @return 年级定义
     */
    SchoolGradeRespVO getSchoolGrade(Long id);

    /**
     * 获得学校年级精简列表
     *
     * @param schoolId 学校编号
     * @return 学校年级精简列表
     */
    List<SchoolGradeSimpleRespVO> getSchoolGradeList(Long schoolId);

    /**
     * 获得 App 学校年级精简列表。
     *
     * @param schoolId 学校编号
     * @return 学校年级精简列表
     */
    List<AppSchoolGradeSimpleRespVO> getAppSchoolGradeSimpleList(Long schoolId);

    // ==================== 子表（学年） ====================

    /**
     * 获得学年分页
     *
     * @param pageReqVO 分页查询
     * @param schoolId 学校ID
     * @return 学年分页
     */
    PageResult<SchoolYearRespVO> getSchoolYearPage(PageParam pageReqVO, Long schoolId);

    /**
     * 创建学年
     *
     * @param schoolYear 创建信息
     * @return 编号
     */
    Long createSchoolYear(@Valid SchoolYearSaveReqVO schoolYear);

    /**
     * 更新学年
     *
     * @param schoolYear 更新信息
     */
    void updateSchoolYear(@Valid SchoolYearSaveReqVO schoolYear);

    /**
     * 删除学年
     *
     * @param id 编号
     */
    void deleteSchoolYear(Long id);

    /**
     * 批量删除学年
     *
     * @param ids 编号
     */
    void deleteSchoolYearListByIds(List<Long> ids);

    /**
     * 获得学年
     *
     * @param id 编号
     * @return 学年
     */
    SchoolYearRespVO getSchoolYear(Long id);

    /**
     * 获得学年精简列表
     *
     * @param schoolId 学校编号
     * @return 学年精简列表
     */
    List<SchoolYearSimpleRespVO> getSchoolYearList(Long schoolId);

    /**
     * 获得 App 可绑定学年精简列表，包含当前学年和未来学年。
     *
     * @param schoolId 学校编号
     * @return 可绑定学年精简列表
     */
    List<AppSchoolYearSimpleRespVO> getAppBindableSchoolYearSimpleList(Long schoolId);

    // ==================== 子表（班级） ====================

    /**
     * 获得班级分页
     *
     * @param pageReqVO 分页查询
     * @param schoolId 学校ID
     * @return 班级分页
     */
    PageResult<SchoolClassRespVO> getSchoolClassPage(PageParam pageReqVO, Long schoolId);

    /**
     * 创建班级
     *
     * @param schoolClass 创建信息
     * @return 编号
     */
    Long createSchoolClass(@Valid SchoolClassSaveReqVO schoolClass);

    /**
     * 更新班级
     *
     * @param schoolClass 更新信息
     */
    void updateSchoolClass(@Valid SchoolClassSaveReqVO schoolClass);

    /**
     * 删除班级
     *
     * @param id 编号
     */
    void deleteSchoolClass(Long id);

    /**
     * 批量删除班级
     *
     * @param ids 编号
     */
    void deleteSchoolClassListByIds(List<Long> ids);

    /**
     * 获得班级
     *
     * @param id 编号
     * @return 班级
     */
    SchoolClassRespVO getSchoolClass(Long id);

    /**
     * 获得班级精简列表
     *
     * @param schoolId 学校编号
     * @param schoolYearId 学年编号
     * @return 班级精简列表
     */
    List<SchoolClassSimpleRespVO> getSchoolClassList(Long schoolId, Long schoolYearId);

    /**
     * 获得 App 学校当前学年班级精简列表。
     *
     * @param schoolId 学校编号
     * @param schoolGradeId 学校年级编号
     * @return 当前学年班级精简列表
     */
    List<AppSchoolClassSimpleRespVO> getAppCurrentSchoolClassSimpleList(Long schoolId, Long schoolGradeId);

    /**
     * 获得 App 指定学年和年级下的班级精简列表。
     *
     * @param schoolId 学校编号
     * @param schoolYearId 学年编号
     * @param schoolGradeId 学校年级编号
     * @return 班级精简列表
     */
    List<AppSchoolClassSimpleRespVO> getAppSchoolClassSimpleList(Long schoolId, Long schoolYearId, Long schoolGradeId);

}
