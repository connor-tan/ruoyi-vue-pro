package cn.iocoder.yudao.module.edu.controller.admin.student;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentClassRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentSaveReqVO;
import cn.iocoder.yudao.module.edu.service.student.StudentService;
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

@Tag(name = "管理后台 - 学生")
@RestController
@RequestMapping("/edu/student")
@Validated
public class StudentController {

    @Resource
    private StudentService studentService;

    @PostMapping("/create")
    @Operation(summary = "创建学生")
    @PreAuthorize("@ss.hasPermission('edu:student:create')")
    public CommonResult<Long> createStudent(@Valid @RequestBody StudentSaveReqVO createReqVO) {
        return success(studentService.createStudent(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新学生")
    @PreAuthorize("@ss.hasPermission('edu:student:update')")
    public CommonResult<Boolean> updateStudent(@Valid @RequestBody StudentSaveReqVO updateReqVO) {
        studentService.updateStudent(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除学生")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('edu:student:delete')")
    public CommonResult<Boolean> deleteStudent(@RequestParam("id") Long id) {
        studentService.deleteStudent(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除学生")
    @PreAuthorize("@ss.hasPermission('edu:student:delete')")
    public CommonResult<Boolean> deleteStudentList(@RequestParam("ids") List<Long> ids) {
        studentService.deleteStudentListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得学生")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('edu:student:query')")
    public CommonResult<StudentRespVO> getStudent(@RequestParam("id") Long id) {
        return success(studentService.getStudent(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得学生分页")
    @PreAuthorize("@ss.hasPermission('edu:student:query')")
    public CommonResult<PageResult<StudentRespVO>> getStudentPage(@Valid StudentPageReqVO pageReqVO) {
        return success(studentService.getStudentPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出学生 Excel")
    @PreAuthorize("@ss.hasPermission('edu:student:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportStudentExcel(@Valid StudentPageReqVO pageReqVO, HttpServletResponse response)
            throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<StudentRespVO> list = studentService.getStudentPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "学生.xls", "数据", StudentRespVO.class, list);
    }

    // ==================== 子表（学生班级区间记录） ====================

    @GetMapping("/student-class/list-by-student-id")
    @Operation(summary = "获得学生班级区间记录列表")
    @Parameter(name = "studentId", description = "学生ID")
    @PreAuthorize("@ss.hasPermission('edu:student:query')")
    public CommonResult<List<StudentClassRespVO>> getStudentClassListByStudentId(@RequestParam("studentId") Long studentId) {
        return success(studentService.getStudentClassListByStudentId(studentId));
    }

}
