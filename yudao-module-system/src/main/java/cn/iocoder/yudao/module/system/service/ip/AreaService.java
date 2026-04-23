package cn.iocoder.yudao.module.system.service.ip;

import cn.iocoder.yudao.module.system.controller.admin.ip.vo.AreaNodeRespVO;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 地区 Service 接口
 */
public interface AreaService {

    /**
     * 获得管理后台地区树
     *
     * @return 地区树
     */
    List<AreaNodeRespVO> getAreaTree();

    /**
     * 获得启用的地区树
     *
     * @param includeAreaId 需要额外包含的地区编号
     * @return 地区树
     */
    default List<AreaNodeRespVO> getEnabledAreaTree(Integer includeAreaId) {
        return getEnabledAreaTree(includeAreaId == null
                ? Collections.emptyList()
                : Collections.singletonList(includeAreaId));
    }

    /**
     * 获得启用的地区树
     *
     * @param includeAreaIds 需要额外包含的地区编号列表
     * @return 地区树
     */
    List<AreaNodeRespVO> getEnabledAreaTree(Collection<Integer> includeAreaIds);

    /**
     * 更新地区状态
     *
     * @param id 地区编号
     * @param status 状态
     */
    void updateAreaStatus(Integer id, Integer status);

    /**
     * 批量更新地区状态
     *
     * @param ids 地区编号列表
     * @param status 状态
     */
    void updateAreaStatusBatch(List<Integer> ids, Integer status);

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
