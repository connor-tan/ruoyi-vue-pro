package cn.iocoder.yudao.module.edu.controller.admin.school;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.GradeCatalogSimpleRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolClassSimpleRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolClassRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolClassSaveReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolGradeRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolGradeSaveReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolGradeSimpleRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolSimpleRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolSaveReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolYearRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolYearSaveReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolYearSimpleRespVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolDO;
import cn.iocoder.yudao.module.edu.service.school.SchoolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 学校信息")
@RestController
@RequestMapping("/edu/school")
@Validated
public class SchoolController {

    @Resource
    private SchoolService schoolService;

    @PostMapping("/create")
    @Operation(summary = "创建学校信息")
    @PreAuthorize("@ss.hasPermission('edu:school:create')")
    public CommonResult<Long> createSchool(@Valid @RequestBody SchoolSaveReqVO createReqVO) {
        return success(schoolService.createSchool(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新学校信息")
    @PreAuthorize("@ss.hasPermission('edu:school:update')")
    public CommonResult<Boolean> updateSchool(@Valid @RequestBody SchoolSaveReqVO updateReqVO) {
        schoolService.updateSchool(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除学校信息")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('edu:school:delete')")
    public CommonResult<Boolean> deleteSchool(@RequestParam("id") Long id) {
        schoolService.deleteSchool(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除学校信息")
    @Parameter(name = "ids", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('edu:school:delete')")
    public CommonResult<Boolean> deleteSchoolList(@RequestParam("ids") List<Long> ids) {
        schoolService.deleteSchoolListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得学校信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('edu:school:query')")
    public CommonResult<SchoolRespVO> getSchool(@RequestParam("id") Long id) {
        return success(buildSchoolResp(schoolService.getSchool(id)));
    }

    @GetMapping("/page")
    @Operation(summary = "获得学校信息分页")
    @PreAuthorize("@ss.hasPermission('edu:school:query')")
    public CommonResult<PageResult<SchoolRespVO>> getSchoolPage(@Valid SchoolPageReqVO pageReqVO) {
        PageResult<SchoolDO> pageResult = schoolService.getSchoolPage(pageReqVO);
        return success(new PageResult<>(buildSchoolRespList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得学校精简列表")
    @PreAuthorize("@ss.hasPermission('edu:school:query')")
    public CommonResult<List<SchoolSimpleRespVO>> getSchoolSimpleList() {
        return success(schoolService.getSchoolSimpleList());
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出学校信息 Excel")
    @PreAuthorize("@ss.hasPermission('edu:school:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSchoolExcel(@Valid SchoolPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<SchoolDO> list = schoolService.getSchoolPage(pageReqVO).getList();
        ExcelUtils.write(response, "学校信息.xls", "数据", SchoolRespVO.class, buildSchoolRespList(list));
    }

    private List<SchoolRespVO> buildSchoolRespList(List<SchoolDO> schools) {
        return BeanUtils.toBean(schools, SchoolRespVO.class, school -> {
            if (school.getAreaId() != null) {
                school.setAreaName(AreaUtils.format(school.getAreaId().intValue()));
            }
        });
    }

    private SchoolRespVO buildSchoolResp(SchoolDO school) {
        if (school == null) {
            return null;
        }
        SchoolRespVO respVO = BeanUtils.toBean(school, SchoolRespVO.class);
        if (respVO.getAreaId() != null) {
            respVO.setAreaName(AreaUtils.format(respVO.getAreaId().intValue()));
        }
        return respVO;
    }

    // ==================== 子表（年级定义） ====================

    @GetMapping("/grade-catalog/simple-list")
    @Operation(summary = "获得年级目录精简列表")
    @PreAuthorize("@ss.hasPermission('edu:school:query')")
    public CommonResult<List<GradeCatalogSimpleRespVO>> getGradeCatalogSimpleList() {
        return success(schoolService.getGradeCatalogList());
    }

    @GetMapping("/school-grade/page")
    @Operation(summary = "获得年级定义分页")
    @Parameter(name = "schoolId", description = "学校ID")
    @PreAuthorize("@ss.hasPermission('edu:school:query')")
    public CommonResult<PageResult<SchoolGradeRespVO>> getSchoolGradePage(
            PageParam pageReqVO,
            @RequestParam("schoolId") Long schoolId) {
        return success(schoolService.getSchoolGradePage(pageReqVO, schoolId));
    }

    @GetMapping("/school-grade/simple-list")
    @Operation(summary = "获得学校年级精简列表")
    @Parameter(name = "schoolId", description = "学校ID", required = true)
    @PreAuthorize("@ss.hasPermission('edu:school:query')")
    public CommonResult<List<SchoolGradeSimpleRespVO>> getSchoolGradeSimpleList(@RequestParam("schoolId") Long schoolId) {
        return success(schoolService.getSchoolGradeList(schoolId));
    }

    @PostMapping("/school-grade/create")
    @Operation(summary = "创建年级定义")
    @PreAuthorize("@ss.hasPermission('edu:school:create')")
    public CommonResult<Long> createSchoolGrade(@Valid @RequestBody SchoolGradeSaveReqVO schoolGrade) {
        return success(schoolService.createSchoolGrade(schoolGrade));
    }

    @PutMapping("/school-grade/update")
    @Operation(summary = "更新年级定义")
    @PreAuthorize("@ss.hasPermission('edu:school:update')")
    public CommonResult<Boolean> updateSchoolGrade(@Valid @RequestBody SchoolGradeSaveReqVO schoolGrade) {
        schoolService.updateSchoolGrade(schoolGrade);
        return success(true);
    }

    @DeleteMapping("/school-grade/delete")
    @Operation(summary = "删除年级定义")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('edu:school:delete')")
    public CommonResult<Boolean> deleteSchoolGrade(@RequestParam("id") Long id) {
        schoolService.deleteSchoolGrade(id);
        return success(true);
    }

    @DeleteMapping("/school-grade/delete-list")
    @Operation(summary = "批量删除年级定义")
    @Parameter(name = "ids", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('edu:school:delete')")
    public CommonResult<Boolean> deleteSchoolGradeList(@RequestParam("ids") List<Long> ids) {
        schoolService.deleteSchoolGradeListByIds(ids);
        return success(true);
    }

    @GetMapping("/school-grade/get")
    @Operation(summary = "获得年级定义")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('edu:school:query')")
    public CommonResult<SchoolGradeRespVO> getSchoolGrade(@RequestParam("id") Long id) {
        return success(schoolService.getSchoolGrade(id));
    }

    // ==================== 子表（学年） ====================

    @GetMapping("/school-year/page")
    @Operation(summary = "获得学年分页")
    @Parameter(name = "schoolId", description = "学校ID")
    @PreAuthorize("@ss.hasPermission('edu:school:query')")
    public CommonResult<PageResult<SchoolYearRespVO>> getSchoolYearPage(
            PageParam pageReqVO,
            @RequestParam("schoolId") Long schoolId) {
        return success(schoolService.getSchoolYearPage(pageReqVO, schoolId));
    }

    @PostMapping("/school-year/create")
    @Operation(summary = "创建学年")
    @PreAuthorize("@ss.hasPermission('edu:school:create')")
    public CommonResult<Long> createSchoolYear(@Valid @RequestBody SchoolYearSaveReqVO schoolYear) {
        return success(schoolService.createSchoolYear(schoolYear));
    }

    @PutMapping("/school-year/update")
    @Operation(summary = "更新学年")
    @PreAuthorize("@ss.hasPermission('edu:school:update')")
    public CommonResult<Boolean> updateSchoolYear(@Valid @RequestBody SchoolYearSaveReqVO schoolYear) {
        schoolService.updateSchoolYear(schoolYear);
        return success(true);
    }

    @DeleteMapping("/school-year/delete")
    @Operation(summary = "删除学年")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('edu:school:delete')")
    public CommonResult<Boolean> deleteSchoolYear(@RequestParam("id") Long id) {
        schoolService.deleteSchoolYear(id);
        return success(true);
    }

    @DeleteMapping("/school-year/delete-list")
    @Operation(summary = "批量删除学年")
    @Parameter(name = "ids", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('edu:school:delete')")
    public CommonResult<Boolean> deleteSchoolYearList(@RequestParam("ids") List<Long> ids) {
        schoolService.deleteSchoolYearListByIds(ids);
        return success(true);
    }

    @GetMapping("/school-year/get")
    @Operation(summary = "获得学年")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('edu:school:query')")
    public CommonResult<SchoolYearRespVO> getSchoolYear(@RequestParam("id") Long id) {
        return success(schoolService.getSchoolYear(id));
    }

    @GetMapping("/school-year/simple-list")
    @Operation(summary = "获得学年精简列表")
    @Parameter(name = "schoolId", description = "学校ID", required = true)
    @PreAuthorize("@ss.hasPermission('edu:school:query')")
    public CommonResult<List<SchoolYearSimpleRespVO>> getSchoolYearSimpleList(@RequestParam("schoolId") Long schoolId) {
        return success(schoolService.getSchoolYearList(schoolId));
    }

    // ==================== 子表（班级） ====================

    @GetMapping("/school-class/page")
    @Operation(summary = "获得班级分页")
    @Parameter(name = "schoolId", description = "学校ID")
    @PreAuthorize("@ss.hasPermission('edu:school:query')")
    public CommonResult<PageResult<SchoolClassRespVO>> getSchoolClassPage(
            PageParam pageReqVO,
            @RequestParam("schoolId") Long schoolId) {
        return success(schoolService.getSchoolClassPage(pageReqVO, schoolId));
    }

    @PostMapping("/school-class/create")
    @Operation(summary = "创建班级")
    @PreAuthorize("@ss.hasPermission('edu:school:create')")
    public CommonResult<Long> createSchoolClass(@Valid @RequestBody SchoolClassSaveReqVO schoolClass) {
        return success(schoolService.createSchoolClass(schoolClass));
    }

    @PutMapping("/school-class/update")
    @Operation(summary = "更新班级")
    @PreAuthorize("@ss.hasPermission('edu:school:update')")
    public CommonResult<Boolean> updateSchoolClass(@Valid @RequestBody SchoolClassSaveReqVO schoolClass) {
        schoolService.updateSchoolClass(schoolClass);
        return success(true);
    }

    @DeleteMapping("/school-class/delete")
    @Operation(summary = "删除班级")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('edu:school:delete')")
    public CommonResult<Boolean> deleteSchoolClass(@RequestParam("id") Long id) {
        schoolService.deleteSchoolClass(id);
        return success(true);
    }

    @DeleteMapping("/school-class/delete-list")
    @Operation(summary = "批量删除班级")
    @Parameter(name = "ids", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('edu:school:delete')")
    public CommonResult<Boolean> deleteSchoolClassList(@RequestParam("ids") List<Long> ids) {
        schoolService.deleteSchoolClassListByIds(ids);
        return success(true);
    }

    @GetMapping("/school-class/get")
    @Operation(summary = "获得班级")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('edu:school:query')")
    public CommonResult<SchoolClassRespVO> getSchoolClass(@RequestParam("id") Long id) {
        return success(schoolService.getSchoolClass(id));
    }

    @GetMapping("/school-class/simple-list")
    @Operation(summary = "获得班级精简列表")
    @Parameter(name = "schoolId", description = "学校ID", required = true)
    @PreAuthorize("@ss.hasPermission('edu:school:query')")
    public CommonResult<List<SchoolClassSimpleRespVO>> getSchoolClassSimpleList(@RequestParam("schoolId") Long schoolId) {
        return success(schoolService.getSchoolClassList(schoolId));
    }

}
