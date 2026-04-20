package cn.iocoder.yudao.module.edu.dal.mysql.school;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.edu.controller.admin.school.vo.SchoolPageReqVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 学校信息 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface SchoolMapper extends BaseMapperX<SchoolDO> {

    default PageResult<SchoolDO> selectPage(SchoolPageReqVO reqVO, List<Long> areaIds, List<Long> stageSchoolIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SchoolDO>()
                .likeIfPresent(SchoolDO::getSchoolName, reqVO.getSchoolName())
                .inIfPresent(SchoolDO::getAreaId, areaIds)
                .inIfPresent(SchoolDO::getId, stageSchoolIds)
                .likeIfPresent(SchoolDO::getSchoolAddress, reqVO.getSchoolAddress())
                .likeIfPresent(SchoolDO::getCode, reqVO.getCode())
                .betweenIfPresent(SchoolDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SchoolDO::getId));
    }

    default List<SchoolDO> selectListByAreaIds(List<Long> areaIds) {
        return selectList(new LambdaQueryWrapperX<SchoolDO>()
                .inIfPresent(SchoolDO::getAreaId, areaIds)
                .orderByAsc(SchoolDO::getId));
    }

}
