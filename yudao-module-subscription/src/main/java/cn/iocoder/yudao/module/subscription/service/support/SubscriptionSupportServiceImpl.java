package cn.iocoder.yudao.module.subscription.service.support;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.SchoolYearDO;
import cn.iocoder.yudao.module.edu.dal.mysql.school.SchoolYearMapper;
import cn.iocoder.yudao.module.subscription.controller.admin.support.vo.SubscriptionSupportSchoolYearSimpleRespVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Validated
public class SubscriptionSupportServiceImpl implements SubscriptionSupportService {

    @Resource
    private SchoolYearMapper schoolYearMapper;

    @Override
    public List<SubscriptionSupportSchoolYearSimpleRespVO> getSchoolYearSimpleList(Long schoolId) {
        List<SchoolYearDO> schoolYears = schoolId != null
                ? schoolYearMapper.selectListBySchoolId(schoolId)
                : schoolYearMapper.selectList(new LambdaQueryWrapperX<SchoolYearDO>()
                .orderByDesc(SchoolYearDO::getYearStart)
                .orderByDesc(SchoolYearDO::getYearEnd)
                .orderByAsc(SchoolYearDO::getId));
        List<SchoolYearDO> uniqueSchoolYears = schoolYears.stream()
                .collect(Collectors.toMap(year -> year.getYearStart() + "-" + year.getYearEnd(),
                        year -> year, (left, right) -> left.getId() <= right.getId() ? left : right,
                        LinkedHashMap::new))
                .values().stream()
                .sorted(Comparator.comparing(SchoolYearDO::getYearStart).reversed()
                        .thenComparing(SchoolYearDO::getYearEnd, Comparator.reverseOrder())
                        .thenComparing(SchoolYearDO::getId))
                .toList();
        return uniqueSchoolYears.stream().map(this::buildSchoolYearSimpleResp).toList();
    }

    @Override
    public SchoolYearDO getSchoolYear(Long id) {
        return id == null ? null : schoolYearMapper.selectById(id);
    }

    @Override
    public Map<Long, SchoolYearDO> getSchoolYearMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return CollectionUtils.convertMap(schoolYearMapper.selectBatchIds(ids), SchoolYearDO::getId);
    }

    private SubscriptionSupportSchoolYearSimpleRespVO buildSchoolYearSimpleResp(SchoolYearDO schoolYear) {
        SubscriptionSupportSchoolYearSimpleRespVO respVO =
                BeanUtils.toBean(schoolYear, SubscriptionSupportSchoolYearSimpleRespVO.class);
        respVO.setName(schoolYear.getYearStart() + "-" + schoolYear.getYearEnd() + "学年");
        return respVO;
    }
}
