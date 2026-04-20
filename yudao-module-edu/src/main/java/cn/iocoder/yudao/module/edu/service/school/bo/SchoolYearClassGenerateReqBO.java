package cn.iocoder.yudao.module.edu.service.school.bo;

import lombok.Data;

/**
 * 学年班级自动生成请求 BO
 */
@Data
public class SchoolYearClassGenerateReqBO {

    /**
     * 目标学年开始年份。为空时使用执行当天所在年份。
     */
    private Integer targetYearStart;

    /**
     * 是否只预览不落库。
     */
    private Boolean dryRun;

}
