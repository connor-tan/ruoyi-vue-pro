package cn.iocoder.yudao.module.edu.service.school.bo;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 学年班级自动生成结果 BO
 */
@Data
public class SchoolYearClassGenerateRespBO {

    private Boolean dryRun;

    private Integer sourceYearStart;

    private Integer sourceYearEnd;

    private Integer targetYearStart;

    private Integer targetYearEnd;

    private Integer processedSchoolCount = 0;

    private Integer skippedSchoolCount = 0;

    private Integer createdYearCount = 0;

    private Integer createdClassCount = 0;

    private Integer skippedClassCount = 0;

    private Map<String, Integer> skipReasonCounts = new LinkedHashMap<>();

}
