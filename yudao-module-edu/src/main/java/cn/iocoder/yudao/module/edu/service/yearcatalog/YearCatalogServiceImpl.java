package cn.iocoder.yudao.module.edu.service.yearcatalog;

import com.baomidou.dynamic.datasource.annotation.Master;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.edu.controller.admin.yearcatalog.vo.YearCatalogPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.yearcatalog.vo.YearCatalogRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.yearcatalog.vo.YearCatalogSaveReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.yearcatalog.vo.YearCatalogSimpleRespVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.YearCatalogDO;
import cn.iocoder.yudao.module.edu.dal.mysql.school.YearCatalogMapper;
import cn.iocoder.yudao.module.edu.service.school.YearCatalogUsageChecker;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.YEAR_CATALOG_DUPLICATE;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.YEAR_CATALOG_IN_USE;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.YEAR_CATALOG_IN_USE_UPDATE;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.YEAR_CATALOG_NOT_EXISTS;
import static cn.iocoder.yudao.module.edu.enums.ErrorCodeConstants.YEAR_CATALOG_RANGE_INVALID;

@Service
@Validated
public class YearCatalogServiceImpl implements YearCatalogService {

    @Resource
    private YearCatalogMapper yearCatalogMapper;
    @Autowired(required = false)
    private List<YearCatalogUsageChecker> usageCheckers = Collections.emptyList();

    @Override
    @Master
    public Long createYearCatalog(YearCatalogSaveReqVO createReqVO) {
        validateYearRange(createReqVO.getYearStart(), createReqVO.getYearEnd());
        validateYearCatalogUnique(null, createReqVO.getYearStart(), createReqVO.getYearEnd());
        YearCatalogDO yearCatalog = BeanUtils.toBean(createReqVO, YearCatalogDO.class);
        yearCatalog.clean();
        yearCatalogMapper.insert(yearCatalog);
        return yearCatalog.getId();
    }

    @Override
    @Master
    public void updateYearCatalog(YearCatalogSaveReqVO updateReqVO) {
        validateYearRange(updateReqVO.getYearStart(), updateReqVO.getYearEnd());
        YearCatalogDO oldYearCatalog = validateYearCatalogExists(updateReqVO.getId());
        validateYearCatalogUnique(updateReqVO.getId(), updateReqVO.getYearStart(), updateReqVO.getYearEnd());
        if ((!Objects.equals(oldYearCatalog.getYearStart(), updateReqVO.getYearStart())
                || !Objects.equals(oldYearCatalog.getYearEnd(), updateReqVO.getYearEnd()))
                && countUsage(oldYearCatalog.getId()) > 0) {
            throw exception(YEAR_CATALOG_IN_USE_UPDATE);
        }
        YearCatalogDO updateObj = BeanUtils.toBean(updateReqVO, YearCatalogDO.class);
        updateObj.clean();
        yearCatalogMapper.updateById(updateObj);
    }

    @Override
    @Master
    public void deleteYearCatalog(Long id) {
        validateYearCatalogExists(id);
        if (countUsage(id) > 0) {
            throw exception(YEAR_CATALOG_IN_USE);
        }
        yearCatalogMapper.deleteById(id);
    }

    @Override
    public YearCatalogRespVO getYearCatalog(Long id) {
        YearCatalogDO yearCatalog = yearCatalogMapper.selectById(id);
        return yearCatalog == null ? null : buildYearCatalogResp(yearCatalog);
    }

    @Override
    public PageResult<YearCatalogRespVO> getYearCatalogPage(YearCatalogPageReqVO pageReqVO) {
        PageResult<YearCatalogDO> pageResult = yearCatalogMapper.selectPage(pageReqVO);
        return new PageResult<>(pageResult.getList().stream().map(this::buildYearCatalogResp).toList(),
                pageResult.getTotal());
    }

    @Override
    public List<YearCatalogSimpleRespVO> getYearCatalogSimpleList() {
        return yearCatalogMapper.selectAllList().stream().map(this::buildYearCatalogSimpleResp).toList();
    }

    @Override
    public YearCatalogDO validateYearCatalogExists(Long id) {
        YearCatalogDO yearCatalog = yearCatalogMapper.selectById(id);
        if (yearCatalog == null) {
            throw exception(YEAR_CATALOG_NOT_EXISTS);
        }
        return yearCatalog;
    }

    @Override
    public Map<Long, YearCatalogDO> getYearCatalogMap(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return convertMap(yearCatalogMapper.selectList(YearCatalogDO::getId, ids), YearCatalogDO::getId);
    }

    private void validateYearRange(Integer yearStart, Integer yearEnd) {
        if (yearStart == null || yearEnd == null || !Objects.equals(yearEnd, yearStart + 1)) {
            throw exception(YEAR_CATALOG_RANGE_INVALID);
        }
    }

    private void validateYearCatalogUnique(Long id, Integer yearStart, Integer yearEnd) {
        YearCatalogDO yearCatalog = yearCatalogMapper.selectByYearRange(yearStart, yearEnd);
        if (yearCatalog == null) {
            return;
        }
        if (id != null && Objects.equals(yearCatalog.getId(), id)) {
            return;
        }
        throw exception(YEAR_CATALOG_DUPLICATE);
    }

    private long countUsage(Long yearCatalogId) {
        return usageCheckers.stream().mapToLong(checker -> checker.countUsage(yearCatalogId)).sum();
    }

    private YearCatalogRespVO buildYearCatalogResp(YearCatalogDO yearCatalog) {
        YearCatalogRespVO respVO = BeanUtils.toBean(yearCatalog, YearCatalogRespVO.class);
        respVO.setName(buildYearCatalogName(yearCatalog.getYearStart(), yearCatalog.getYearEnd()));
        return respVO;
    }

    private YearCatalogSimpleRespVO buildYearCatalogSimpleResp(YearCatalogDO yearCatalog) {
        YearCatalogSimpleRespVO respVO = BeanUtils.toBean(yearCatalog, YearCatalogSimpleRespVO.class);
        respVO.setName(buildYearCatalogName(yearCatalog.getYearStart(), yearCatalog.getYearEnd()));
        return respVO;
    }

    private String buildYearCatalogName(Integer yearStart, Integer yearEnd) {
        return yearStart == null || yearEnd == null ? null : yearStart + "-" + yearEnd + "学年";
    }
}
