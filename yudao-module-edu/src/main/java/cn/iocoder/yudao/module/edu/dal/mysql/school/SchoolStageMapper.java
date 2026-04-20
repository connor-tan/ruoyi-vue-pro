package cn.iocoder.yudao.module.edu.dal.mysql.school;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolStageDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 学校办学学段 Mapper
 */
@Mapper
public interface SchoolStageMapper extends BaseMapperX<SchoolStageDO> {

    default List<SchoolStageDO> selectListBySchoolId(Long schoolId) {
        return selectList(new LambdaQueryWrapperX<SchoolStageDO>()
                .eq(SchoolStageDO::getSchoolId, schoolId)
                .orderByAsc(SchoolStageDO::getId));
    }

    default List<SchoolStageDO> selectListBySchoolIds(Collection<Long> schoolIds) {
        if (CollUtil.isEmpty(schoolIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<SchoolStageDO>()
                .in(SchoolStageDO::getSchoolId, schoolIds)
                .orderByAsc(SchoolStageDO::getSchoolId)
                .orderByAsc(SchoolStageDO::getId));
    }

    default List<Long> selectSchoolIdsByStage(String stage) {
        if (StrUtil.isBlank(stage)) {
            return Collections.emptyList();
        }
        return convertList(selectList(new LambdaQueryWrapperX<SchoolStageDO>()
                .eq(SchoolStageDO::getStage, stage)), SchoolStageDO::getSchoolId);
    }

    @Delete("DELETE FROM edu_school_stage WHERE school_id = #{schoolId}")
    void deleteBySchoolId(@Param("schoolId") Long schoolId);

    @Delete("""
            <script>
            DELETE FROM edu_school_stage
            WHERE school_id IN
            <foreach collection="schoolIds" item="schoolId" open="(" separator="," close=")">
                #{schoolId}
            </foreach>
            </script>
            """)
    void deleteBySchoolIds(@Param("schoolIds") Collection<Long> schoolIds);

}
