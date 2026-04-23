package cn.iocoder.yudao.module.edu.controller.app.student;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.edu.controller.app.student.vo.AppStudentSimpleRespVO;
import cn.iocoder.yudao.module.edu.service.student.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 学生")
@RestController
@RequestMapping("/edu/student")
@Validated
public class AppStudentController {

    @Resource
    private StudentService studentService;

    @GetMapping("/my-simple-list")
    @Operation(summary = "获得当前家长绑定的学生精简列表")
    public CommonResult<List<AppStudentSimpleRespVO>> getMySimpleList() {
        return success(studentService.getAppStudentSimpleList(getLoginUserId()));
    }
}
