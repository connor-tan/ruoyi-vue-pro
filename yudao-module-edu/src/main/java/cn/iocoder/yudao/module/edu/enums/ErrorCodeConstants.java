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
    ErrorCode SCHOOL_IN_USE_BY_STUDENT = new ErrorCode(1_016_001_015, "学校已被学生引用，无法删除");
    ErrorCode SCHOOL_CLASS_IN_USE_BY_STUDENT = new ErrorCode(1_016_001_016, "班级已被学生引用，无法删除");

    ErrorCode STUDENT_NOT_EXISTS = new ErrorCode(1_017_001_001, "学生不存在");
    ErrorCode STUDENT_PARENT_NOT_EXISTS = new ErrorCode(1_017_001_002, "家长会员不存在");
    ErrorCode STUDENT_CLASS_RECORD_NOT_EXISTS = new ErrorCode(1_017_001_003, "学生班级记录不存在");
    ErrorCode STUDENT_CLASS_NOT_BELONG_TO_SCHOOL = new ErrorCode(1_017_001_004, "学生班级不属于当前学校");
    ErrorCode STUDENT_CLASS_ENTRY_YEAR_NOT_MATCH = new ErrorCode(1_017_001_005, "学生班级入学批次与学生入学年不一致");
    ErrorCode STUDENT_CLASS_END_DATE_INVALID = new ErrorCode(1_017_001_006, "学生离班日期不能早于入班日期");
    ErrorCode STUDENT_CLASS_MULTI_CURRENT = new ErrorCode(1_017_001_007, "学生班级记录最多只能有一条当前记录");
    ErrorCode STUDENT_CLASS_DUPLICATE_START_DATE = new ErrorCode(1_017_001_008, "学生班级记录入班日期不能重复");
    ErrorCode STUDENT_CLASS_DATE_OVERLAP = new ErrorCode(1_017_001_009, "学生班级记录时间区间不能重叠");
    ErrorCode STUDENT_PROMOTION_TARGET_SCHOOL_YEAR_INVALID = new ErrorCode(1_017_001_010, "目标学年必须是来源学年的下一学年");
    ErrorCode STUDENT_PROMOTION_TARGET_YEAR_INVALID = new ErrorCode(1_017_001_011, "目标学年必须是来源学年的下一学年");
    ErrorCode STUDENT_PROMOTION_SCOPE_INVALID = new ErrorCode(1_017_001_012, "学校范围类型不正确");
    ErrorCode STUDENT_PROMOTION_SCHOOL_IDS_EMPTY = new ErrorCode(1_017_001_013, "请选择要执行升班的学校");
    ErrorCode STUDENT_PROMOTION_ADJUST_ACTION_INVALID = new ErrorCode(1_017_001_014, "学生升班调整动作不正确");
    ErrorCode STUDENT_PROMOTION_ADJUST_TARGET_CLASS_REQUIRED = new ErrorCode(1_017_001_015, "学生升班调整必须指定目标班级");
    ErrorCode STUDENT_PROMOTION_ADJUST_TARGET_CLASS_INVALID = new ErrorCode(1_017_001_016, "学生升班调整的目标班级不正确");
    ErrorCode STUDENT_PROMOTION_ADJUST_TARGET_CLASS_NOT_IN_TARGET_YEAR = new ErrorCode(1_017_001_017, "学生升班调整的目标班级不属于目标学年");
    ErrorCode STUDENT_PROMOTION_TASK_NOT_EXISTS = new ErrorCode(1_017_001_018, "升班任务不存在");
    ErrorCode STUDENT_PROMOTION_TASK_NOT_ROLLBACKABLE = new ErrorCode(1_017_001_019, "当前升班任务不允许回滚");
    ErrorCode STUDENT_PROMOTION_TASK_ROLLBACK_STATE_INVALID = new ErrorCode(1_017_001_020, "当前升班任务状态已变化，无法回滚");
    ErrorCode STUDENT_PROMOTION_NO_ELIGIBLE_STUDENTS = new ErrorCode(1_017_001_021, "当前没有符合升班条件的学生");
    ErrorCode STUDENT_IN_USE_BY_FLOW = new ErrorCode(1_017_001_022, "学生已有升班或流转记录，无法删除");
    ErrorCode STUDENT_STATUS_READING_CURRENT_CLASS_REQUIRED = new ErrorCode(1_017_001_023, "在读学生必须存在一条当前班级记录");
    ErrorCode STUDENT_STATUS_CURRENT_CLASS_FORBIDDEN = new ErrorCode(1_017_001_024, "非在读学生不能存在当前班级记录");
}
