package cn.iocoder.yudao.module.edu.service.school;

import cn.iocoder.yudao.module.edu.service.school.bo.SchoolYearClassGenerateReqBO;
import cn.iocoder.yudao.module.edu.service.school.bo.SchoolYearClassGenerateRespBO;

/**
 * 学年班级自动生成 Service
 */
public interface SchoolYearClassGenerateService {

    /**
     * 生成目标学年和班级。
     *
     * @param reqBO 生成请求
     * @return 生成结果
     */
    SchoolYearClassGenerateRespBO generate(SchoolYearClassGenerateReqBO reqBO);

}
