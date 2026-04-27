package cn.iocoder.yudao.module.edu.dal.mysql.school;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolClassDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 班级 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface SchoolClassMapper extends BaseMapperX<SchoolClassDO> {

    default PageResult<SchoolClassDO> selectPage(PageParam reqVO, Long schoolId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SchoolClassDO>()
            .eq(SchoolClassDO::getSchoolId, schoolId)
            .orderByDesc(SchoolClassDO::getId));
    }

    default List<SchoolClassDO> selectListBySchoolId(Long schoolId) {
        return selectList(new LambdaQueryWrapperX<SchoolClassDO>()
                .eq(SchoolClassDO::getSchoolId, schoolId)
                .orderByDesc(SchoolClassDO::getEntryYear)
                .orderByAsc(SchoolClassDO::getClassNo)
                .orderByDesc(SchoolClassDO::getId));
    }

    default List<SchoolClassDO> selectListBySchoolIdAndSchoolYearId(Long schoolId, Long schoolYearId) {
        return selectList(new LambdaQueryWrapperX<SchoolClassDO>()
                .eq(SchoolClassDO::getSchoolId, schoolId)
                .eq(SchoolClassDO::getSchoolYearId, schoolYearId)
                .orderByDesc(SchoolClassDO::getEntryYear)
                .orderByAsc(SchoolClassDO::getClassNo)
                .orderByDesc(SchoolClassDO::getId));
    }

    default List<SchoolClassDO> selectListBySchoolYearIds(Collection<Long> schoolYearIds) {
        if (CollUtil.isEmpty(schoolYearIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<SchoolClassDO>()
                .in(SchoolClassDO::getSchoolYearId, schoolYearIds)
                .orderByAsc(SchoolClassDO::getSchoolId)
                .orderByAsc(SchoolClassDO::getSchoolYearId)
                .orderByAsc(SchoolClassDO::getSchoolGradeId)
                .orderByAsc(SchoolClassDO::getClassNo)
                .orderByAsc(SchoolClassDO::getId));
    }

    default SchoolClassDO selectByUniqueKey(Long schoolId, Integer entryYear, Long schoolYearId,
                                            Long schoolGradeId, Integer classNo) {
        return selectOne(new LambdaQueryWrapperX<SchoolClassDO>()
                .eq(SchoolClassDO::getSchoolId, schoolId)
                .eq(SchoolClassDO::getEntryYear, entryYear)
                .eq(SchoolClassDO::getSchoolYearId, schoolYearId)
                .eq(SchoolClassDO::getSchoolGradeId, schoolGradeId)
                .eq(SchoolClassDO::getClassNo, classNo));
    }

    default Long countBySchoolGradeId(Long schoolGradeId) {
        return selectCount(SchoolClassDO::getSchoolGradeId, schoolGradeId);
    }

    default Long countBySchoolYearId(Long schoolYearId) {
        return selectCount(SchoolClassDO::getSchoolYearId, schoolYearId);
    }

    int deletePhysicallyById(@Param("id") Long id);

    int deletePhysicallyByIds(@Param("ids") Collection<Long> ids);

    int deletePhysicallyBySchoolId(@Param("schoolId") Long schoolId);

    int deletePhysicallyBySchoolIds(@Param("schoolIds") Collection<Long> schoolIds);

}
