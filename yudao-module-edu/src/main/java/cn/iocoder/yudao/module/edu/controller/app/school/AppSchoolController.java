package cn.iocoder.yudao.module.edu.controller.app.school;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.edu.controller.app.school.vo.AppSchoolClassSimpleRespVO;
import cn.iocoder.yudao.module.edu.controller.app.school.vo.AppSchoolGradeSimpleRespVO;
import cn.iocoder.yudao.module.edu.controller.app.school.vo.AppSchoolSimpleRespVO;
import cn.iocoder.yudao.module.edu.controller.app.school.vo.AppSchoolYearSimpleRespVO;
import cn.iocoder.yudao.module.edu.service.school.SchoolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 学校")
@RestController
@RequestMapping("/edu/school")
@Validated
public class AppSchoolController {

    @Resource
    private SchoolService schoolService;

    @GetMapping("/simple-list")
    @Operation(summary = "获得学校精简列表")
    @Parameter(name = "areaId", description = "地区编号，传区级地区时会包含其可选下级地区", example = "320214")
    public CommonResult<List<AppSchoolSimpleRespVO>> getSchoolSimpleList(
            @RequestParam(value = "areaId", required = false) Long areaId) {
        return success(schoolService.getAppSchoolSimpleList(areaId));
    }

    @GetMapping("/school-grade/simple-list")
    @Operation(summary = "获得学校年级精简列表")
    @Parameter(name = "schoolId", description = "学校编号", required = true, example = "1")
    public CommonResult<List<AppSchoolGradeSimpleRespVO>> getSchoolGradeSimpleList(
            @RequestParam("schoolId") Long schoolId) {
        return success(schoolService.getAppSchoolGradeSimpleList(schoolId));
    }

    @GetMapping("/school-year/bindable-simple-list")
    @Operation(summary = "获得学校可绑定学年精简列表")
    @Parameter(name = "schoolId", description = "学校编号", required = true, example = "1")
    public CommonResult<List<AppSchoolYearSimpleRespVO>> getBindableSchoolYearSimpleList(
            @RequestParam("schoolId") Long schoolId) {
        return success(schoolService.getAppBindableSchoolYearSimpleList(schoolId));
    }

    @GetMapping("/school-class/current-simple-list")
    @Operation(summary = "获得学校当前学年班级精简列表")
    public CommonResult<List<AppSchoolClassSimpleRespVO>> getCurrentSchoolClassSimpleList(
            @RequestParam("schoolId") Long schoolId,
            @RequestParam("schoolGradeId") Long schoolGradeId) {
        return success(schoolService.getAppCurrentSchoolClassSimpleList(schoolId, schoolGradeId));
    }

    @GetMapping("/school-class/simple-list")
    @Operation(summary = "获得学校指定学年班级精简列表")
    public CommonResult<List<AppSchoolClassSimpleRespVO>> getSchoolClassSimpleList(
            @RequestParam("schoolId") Long schoolId,
            @RequestParam("schoolYearId") Long schoolYearId,
            @RequestParam("schoolGradeId") Long schoolGradeId) {
        return success(schoolService.getAppSchoolClassSimpleList(schoolId, schoolYearId, schoolGradeId));
    }

}
