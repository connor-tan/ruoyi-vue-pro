package cn.iocoder.yudao.module.system.api.ip;

import java.util.Collection;
import java.util.List;

/**
 * 地区 API 接口
 */
public interface AreaApi {

    /**
     * 校验地区是否可选
     *
     * @param areaId 地区编号
     */
    void validateAreaSelectable(Integer areaId);

    /**
     * 批量校验地区是否可选
     *
     * @param areaIds 地区编号列表
     */
    void validateAreaSelectableList(Collection<Integer> areaIds);

    /**
     * 获得可选地区及其可选子地区编号列表
     *
     * @param areaId 地区编号
     * @return 可选地区编号列表
     */
    List<Integer> getSelectableAreaIds(Integer areaId);

}
