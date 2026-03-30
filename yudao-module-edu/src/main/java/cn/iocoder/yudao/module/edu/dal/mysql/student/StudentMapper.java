package cn.iocoder.yudao.module.edu.dal.mysql.student;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.StudentPageReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生 Mapper
 *
 * @author connor
 */
@Mapper
public interface StudentMapper extends BaseMapperX<StudentDO> {

    default PageResult<StudentDO> selectPage(StudentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<StudentDO>()
                .likeIfPresent(StudentDO::getStudentName, reqVO.getStudentName())
                .eqIfPresent(StudentDO::getBelongTo, reqVO.getBelongTo())
                .eqIfPresent(StudentDO::getCurrentSchoolId, reqVO.getCurrentSchoolId())
                .eqIfPresent(StudentDO::getEntryYear, reqVO.getEntryYear())
                .eqIfPresent(StudentDO::getStudentCode, reqVO.getStudentCode())
                .eqIfPresent(StudentDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(StudentDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(StudentDO::getId));
    }

}
