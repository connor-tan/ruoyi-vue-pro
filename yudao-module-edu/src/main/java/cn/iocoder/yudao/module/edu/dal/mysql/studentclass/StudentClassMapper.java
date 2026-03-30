package cn.iocoder.yudao.module.edu.dal.mysql.studentclass;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.dal.dataobject.studentclass.StudentClassDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 学生班级记录 Mapper
 *
 * @author connor
 */
@Mapper
public interface StudentClassMapper extends BaseMapperX<StudentClassDO> {

    default List<StudentClassDO> selectListByStudentId(Long studentId) {
        return selectList(new LambdaQueryWrapperX<StudentClassDO>()
                .eq(StudentClassDO::getStudentId, studentId)
                .orderByAsc(StudentClassDO::getStartDate)
                .orderByAsc(StudentClassDO::getId));
    }

    default int deleteByStudentId(Long studentId) {
        return delete(StudentClassDO::getStudentId, studentId);
    }

    default int deleteByStudentIds(List<Long> studentIds) {
        return deleteBatch(StudentClassDO::getStudentId, studentIds);
    }

}
