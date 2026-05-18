package cn.iocoder.yudao.module.edu.job;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.module.edu.service.student.StudentService;
import cn.iocoder.yudao.module.edu.service.student.bo.StudentWaitingEntryActivateRespBO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 待入学学生自动转在读 Job。
 */
@Component
@Slf4j
public class EduStudentWaitingEntryActivateJob implements JobHandler {

    @Resource
    private StudentService studentService;

    @Override
    public String execute(String param) {
        StudentWaitingEntryActivateRespBO result = studentService.activateWaitingEntryStudents();
        String resultJson = JsonUtils.toJsonString(result);
        log.info("[execute][待入学学生自动转在读完成 result({})]", resultJson);
        return resultJson;
    }

}
