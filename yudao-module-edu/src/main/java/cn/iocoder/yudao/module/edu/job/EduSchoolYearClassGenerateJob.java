package cn.iocoder.yudao.module.edu.job;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.module.edu.service.school.SchoolYearClassGenerateService;
import cn.iocoder.yudao.module.edu.service.school.bo.SchoolYearClassGenerateReqBO;
import cn.iocoder.yudao.module.edu.service.school.bo.SchoolYearClassGenerateRespBO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 学年班级自动生成 Job。
 */
@Component
@Slf4j
public class EduSchoolYearClassGenerateJob implements JobHandler {

    @Resource
    private SchoolYearClassGenerateService schoolYearClassGenerateService;

    @Override
    public String execute(String param) {
        SchoolYearClassGenerateReqBO reqBO = parseParam(param);
        SchoolYearClassGenerateRespBO respBO = schoolYearClassGenerateService.generate(reqBO);
        String result = JsonUtils.toJsonString(respBO);
        log.info("[execute][学年班级自动生成完成 result({})]", result);
        return result;
    }

    private SchoolYearClassGenerateReqBO parseParam(String param) {
        if (StrUtil.isBlank(param)) {
            return new SchoolYearClassGenerateReqBO();
        }
        SchoolYearClassGenerateReqBO reqBO = JsonUtils.parseObject(param, SchoolYearClassGenerateReqBO.class);
        return reqBO == null ? new SchoolYearClassGenerateReqBO() : reqBO;
    }

}
