package cn.iocoder.yudao.module.edu.service.yearcatalog;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.edu.controller.admin.yearcatalog.vo.YearCatalogPageReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.yearcatalog.vo.YearCatalogRespVO;
import cn.iocoder.yudao.module.edu.controller.admin.yearcatalog.vo.YearCatalogSaveReqVO;
import cn.iocoder.yudao.module.edu.controller.admin.yearcatalog.vo.YearCatalogSimpleRespVO;
import cn.iocoder.yudao.module.edu.dal.dataobject.school.YearCatalogDO;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

public interface YearCatalogService {

    Long createYearCatalog(@Valid YearCatalogSaveReqVO createReqVO);

    void updateYearCatalog(@Valid YearCatalogSaveReqVO updateReqVO);

    void deleteYearCatalog(Long id);

    YearCatalogRespVO getYearCatalog(Long id);

    PageResult<YearCatalogRespVO> getYearCatalogPage(YearCatalogPageReqVO pageReqVO);

    List<YearCatalogSimpleRespVO> getYearCatalogSimpleList();

    YearCatalogDO validateYearCatalogExists(Long id);

    Map<Long, YearCatalogDO> getYearCatalogMap(List<Long> ids);
}
