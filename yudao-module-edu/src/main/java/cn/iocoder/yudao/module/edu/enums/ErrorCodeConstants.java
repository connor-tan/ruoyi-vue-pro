package cn.iocoder.yudao.module.edu.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

public interface ErrorCodeConstants {

    ErrorCode SCHOOL_NOT_EXISTS = new ErrorCode(1_016_001_001, "学校信息不存在");
    ErrorCode SCHOOL_GRADE_NOT_EXISTS = new ErrorCode(1_016_001_002, "年级定义不存在");
    ErrorCode SCHOOL_YEAR_NOT_EXISTS = new ErrorCode(1_016_001_003, "学年不存在");
    ErrorCode SCHOOL_CLASS_NOT_EXISTS = new ErrorCode(1_016_001_004, "班级不存在");
    ErrorCode GRADE_CATALOG_NOT_EXISTS = new ErrorCode(1_016_001_005, "年级目录不存在");
    ErrorCode GRADE_CATALOG_DISABLED = new ErrorCode(1_016_001_006, "年级目录已停用");
    ErrorCode SCHOOL_GRADE_DUPLICATE = new ErrorCode(1_016_001_007, "学校年级已存在");
    ErrorCode SCHOOL_GRADE_IN_USE = new ErrorCode(1_016_001_008, "学校年级已被班级引用，无法删除");
    ErrorCode SCHOOL_YEAR_IN_USE = new ErrorCode(1_016_001_009, "学年已被班级引用，无法删除");
    ErrorCode SCHOOL_GRADE_NOT_BELONG_TO_SCHOOL = new ErrorCode(1_016_001_010, "学校年级不属于当前学校");
    ErrorCode SCHOOL_YEAR_NOT_BELONG_TO_SCHOOL = new ErrorCode(1_016_001_011, "学年不属于当前学校");
    ErrorCode SCHOOL_CLASS_DUPLICATE = new ErrorCode(1_016_001_012, "班级已存在");
    ErrorCode SCHOOL_YEAR_DUPLICATE = new ErrorCode(1_016_001_013, "学校学年已存在");
    ErrorCode SCHOOL_GRADE_IN_USE_UPDATE = new ErrorCode(1_016_001_014, "学校年级已被班级引用，无法修改");

    ErrorCode STUDENT_NOT_EXISTS = new ErrorCode(1_017_001_001, "学生不存在");
    ErrorCode STUDENT_PARENT_NOT_EXISTS = new ErrorCode(1_017_001_002, "家长会员不存在");
    ErrorCode STUDENT_CLASS_RECORD_NOT_EXISTS = new ErrorCode(1_017_001_003, "学生班级记录不存在");
    ErrorCode STUDENT_CLASS_NOT_BELONG_TO_SCHOOL = new ErrorCode(1_017_001_004, "学生班级不属于当前学校");
    ErrorCode STUDENT_CLASS_ENTRY_YEAR_NOT_MATCH = new ErrorCode(1_017_001_005, "学生班级入学批次与学生入学年不一致");
    ErrorCode STUDENT_CLASS_END_DATE_INVALID = new ErrorCode(1_017_001_006, "学生离班日期不能早于入班日期");
    ErrorCode STUDENT_CLASS_MULTI_CURRENT = new ErrorCode(1_017_001_007, "学生班级记录最多只能有一条当前记录");
    ErrorCode STUDENT_CLASS_DUPLICATE_START_DATE = new ErrorCode(1_017_001_008, "学生班级记录入班日期不能重复");
    ErrorCode STUDENT_CLASS_DATE_OVERLAP = new ErrorCode(1_017_001_009, "学生班级记录时间区间不能重叠");
}
