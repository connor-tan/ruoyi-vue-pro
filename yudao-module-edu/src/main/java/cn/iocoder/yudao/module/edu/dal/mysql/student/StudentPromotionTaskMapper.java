package cn.iocoder.yudao.module.edu.dal.mysql.student;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.controller.admin.student.vo.promotion.StudentPromotionTaskPageReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.student.StudentPromotionTaskDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudentPromotionTaskMapper extends BaseMapperX<StudentPromotionTaskDO> {

    default PageResult<StudentPromotionTaskDO> selectPage(StudentPromotionTaskPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<StudentPromotionTaskDO>()
                .eqIfPresent(StudentPromotionTaskDO::getId, reqVO.getId())
                .eqIfPresent(StudentPromotionTaskDO::getFromYearStart, reqVO.getFromYearStart())
                .eqIfPresent(StudentPromotionTaskDO::getToYearStart, reqVO.getToYearStart())
                .eqIfPresent(StudentPromotionTaskDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(StudentPromotionTaskDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(StudentPromotionTaskDO::getId));
    }
}
