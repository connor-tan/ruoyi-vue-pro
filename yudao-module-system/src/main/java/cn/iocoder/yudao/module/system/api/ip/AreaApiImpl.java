package cn.iocoder.yudao.module.system.api.ip;

import cn.iocoder.yudao.module.system.service.ip.AreaService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

/**
 * 地区 API 实现类
 */
@Service
@Validated
public class AreaApiImpl implements AreaApi {

    @Resource
    private AreaService areaService;

    @Override
    public void validateAreaSelectable(Integer areaId) {
        areaService.validateAreaSelectable(areaId);
    }

    @Override
    public void validateAreaSelectableList(Collection<Integer> areaIds) {
        areaService.validateAreaSelectableList(areaIds);
    }

    @Override
    public List<Integer> getSelectableAreaIds(Integer areaId) {
        return areaService.getSelectableAreaIds(areaId);
    }

}
