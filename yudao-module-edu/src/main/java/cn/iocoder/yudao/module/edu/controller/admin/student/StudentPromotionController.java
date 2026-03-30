package cn.iocoder.yudao.module.edu.controller.admin.student;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentGlobalPromotionExecuteReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentGlobalPromotionExecuteRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentGlobalPromotionPreviewReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentGlobalPromotionPreviewRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentGlobalPromotionRollbackReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentGlobalPromotionRollbackRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentFlowPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentFlowRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionBatchRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionExecuteReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionExecuteRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionPreviewReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionPreviewRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionTaskPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionTaskRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionYearOptionRespVO;
import cn.iocoder.yudao.module.edu.service.student.StudentPromotionService;
import cn.iocoder.yudao.module.edu.service.student.StudentPromotionTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 学生一键升班")
@RestController
@RequestMapping("/edu/student/promotion")
@Validated
public class StudentPromotionController {

    @Resource
    private StudentPromotionService studentPromotionService;
    @Resource
    private StudentPromotionTaskService studentPromotionTaskService;

    @GetMapping("/year-options")
    @Operation(summary = "获得全局批量升班可选学年")
    @PreAuthorize("@ss.hasPermission('edu:student-promotion:query')")
    public CommonResult<List<StudentPromotionYearOptionRespVO>> getPromotionYearOptions() {
        return success(studentPromotionTaskService.getPromotionYearOptions());
    }

    @GetMapping("/task/page")
    @Operation(summary = "获得升班任务分页")
    @PreAuthorize("@ss.hasPermission('edu:student-promotion:query')")
    public CommonResult<PageResult<StudentPromotionTaskRespVO>> getPromotionTaskPage(
            @Valid StudentPromotionTaskPageReqVO pageReqVO) {
        return success(studentPromotionTaskService.getPromotionTaskPage(pageReqVO));
    }

    @GetMapping("/task-batch/list")
    @Operation(summary = "获得升班任务下的学校批次列表")
    @PreAuthorize("@ss.hasPermission('edu:student-promotion:query')")
    public CommonResult<List<StudentPromotionBatchRespVO>> getPromotionBatchListByTaskId(
            @RequestParam("taskId") Long taskId) {
        return success(studentPromotionTaskService.getPromotionBatchListByTaskId(taskId));
    }

    @GetMapping("/flow/page")
    @Operation(summary = "获得学生流转分页")
    @PreAuthorize("@ss.hasPermission('edu:student-promotion:query')")
    public CommonResult<PageResult<StudentFlowRespVO>> getStudentFlowPage(@Valid StudentFlowPageReqVO pageReqVO) {
        return success(studentPromotionTaskService.getStudentFlowPage(pageReqVO));
    }

    @PostMapping("/preview")
    @Operation(summary = "预览学生一键升班")
    @PreAuthorize("@ss.hasPermission('edu:student:update')")
    public CommonResult<StudentPromotionPreviewRespVO> previewStudentPromotion(
            @Valid @RequestBody StudentPromotionPreviewReqVO reqVO) {
        return success(studentPromotionService.previewStudentPromotion(reqVO));
    }

    @PostMapping("/execute")
    @Operation(summary = "执行学生一键升班")
    @PreAuthorize("@ss.hasPermission('edu:student:update')")
    public CommonResult<StudentPromotionExecuteRespVO> executeStudentPromotion(
            @Valid @RequestBody StudentPromotionExecuteReqVO reqVO) {
        return success(studentPromotionService.executeStudentPromotion(reqVO));
    }

    @PostMapping("/global-preview")
    @Operation(summary = "预览学生全局批量升班")
    @PreAuthorize("@ss.hasPermission('edu:student-promotion:query')")
    public CommonResult<StudentGlobalPromotionPreviewRespVO> previewGlobalStudentPromotion(
            @Valid @RequestBody StudentGlobalPromotionPreviewReqVO reqVO) {
        return success(studentPromotionTaskService.previewGlobalStudentPromotion(reqVO));
    }

    @PostMapping("/global-execute")
    @Operation(summary = "执行学生全局批量升班")
    @PreAuthorize("@ss.hasPermission('edu:student-promotion:update')")
    public CommonResult<StudentGlobalPromotionExecuteRespVO> executeGlobalStudentPromotion(
            @Valid @RequestBody StudentGlobalPromotionExecuteReqVO reqVO) {
        return success(studentPromotionTaskService.executeGlobalStudentPromotion(reqVO));
    }

    @PostMapping("/global-rollback")
    @Operation(summary = "回滚学生全局批量升班任务")
    @PreAuthorize("@ss.hasPermission('edu:student-promotion:update')")
    public CommonResult<StudentGlobalPromotionRollbackRespVO> rollbackGlobalStudentPromotion(
            @Valid @RequestBody StudentGlobalPromotionRollbackReqVO reqVO) {
        return success(studentPromotionTaskService.rollbackGlobalStudentPromotion(reqVO));
    }

}
