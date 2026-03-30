package cn.iocoder.yudao.module.system.service.ip;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.ip.core.Area;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.system.controller.admin.ip.vo.AreaNodeRespVO;
import cn.iocoder.yudao.module.system.dal.dataobject.ip.AreaConfigDO;
import cn.iocoder.yudao.module.system.dal.mysql.ip.AreaConfigMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.AREA_NOT_EXISTS;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.AREA_NOT_SELECTABLE;

/**
 * 地区 Service 实现类
 */
@Service
@Validated
public class AreaServiceImpl implements AreaService {

    @Resource
    private AreaConfigMapper areaConfigMapper;

    @Override
    public List<AreaNodeRespVO> getAreaTree() {
        return buildAreaTree(getChinaChildren(), getAreaStatusMap());
    }

    @Override
    public List<AreaNodeRespVO> getEnabledAreaTree(Collection<Integer> includeAreaIds) {
        return buildEnabledAreaTree(getChinaChildren(), getAreaStatusMap(), true,
                buildIncludeAreaIds(includeAreaIds));
    }

    @Override
    public void updateAreaStatus(Integer id, Integer status) {
        validateAreaExists(id);
        AreaConfigDO areaConfig = areaConfigMapper.selectByAreaId(id);
        if (areaConfig == null) {
            areaConfig = new AreaConfigDO();
            areaConfig.setAreaId(id);
            areaConfig.setStatus(status);
            areaConfigMapper.insert(areaConfig);
            return;
        }
        if (ObjUtil.equal(areaConfig.getStatus(), status)) {
            return;
        }
        areaConfig.setStatus(status);
        areaConfig.clean();
        areaConfigMapper.updateById(areaConfig);
    }

    @Override
    public void updateAreaStatusBatch(List<Integer> ids, Integer status) {
        ids.stream().distinct().forEach(id -> updateAreaStatus(id, status));
    }

    @Override
    public void validateAreaSelectable(Integer areaId) {
        if (!isAreaSelectable(areaId)) {
            throw exception(AREA_NOT_SELECTABLE);
        }
    }

    @Override
    public void validateAreaSelectableList(Collection<Integer> areaIds) {
        if (CollUtil.isEmpty(areaIds)) {
            return;
        }
        Map<Integer, Integer> areaStatusMap = getAreaStatusMap();
        areaIds.stream().distinct().forEach(areaId -> {
            if (!isAreaSelectable(areaId, areaStatusMap)) {
                throw exception(AREA_NOT_SELECTABLE);
            }
        });
    }

    @Override
    public List<Integer> getSelectableAreaIds(Integer areaId) {
        if (areaId == null) {
            return Collections.emptyList();
        }
        Map<Integer, Integer> areaStatusMap = getAreaStatusMap();
        Area root = getRequiredArea(areaId);
        if (!isAreaSelectable(root, areaStatusMap)) {
            throw exception(AREA_NOT_SELECTABLE);
        }
        List<Integer> areaIds = new ArrayList<>();
        collectSelectableAreaIds(root, areaStatusMap, areaIds);
        return areaIds;
    }

    private boolean isAreaSelectable(Integer areaId) {
        return isAreaSelectable(areaId, getAreaStatusMap());
    }

    private boolean isAreaSelectable(Integer areaId, Map<Integer, Integer> areaStatusMap) {
        Area area = getRequiredArea(areaId);
        return isAreaSelectable(area, areaStatusMap);
    }

    private boolean isAreaSelectable(Area area, Map<Integer, Integer> areaStatusMap) {
        while (area != null && !ObjUtil.equal(area.getId(), Area.ID_CHINA) && !ObjUtil.equal(area.getId(), Area.ID_GLOBAL)) {
            Integer status = areaStatusMap.get(area.getId());
            if (status != null && !CommonStatusEnum.isEnable(status)) {
                return false;
            }
            area = area.getParent();
        }
        return true;
    }

    private void validateAreaExists(Integer id) {
        getRequiredArea(id);
    }

    private void collectSelectableAreaIds(Area area, Map<Integer, Integer> areaStatusMap, List<Integer> areaIds) {
        Integer status = areaStatusMap.get(area.getId());
        if (status != null && !CommonStatusEnum.isEnable(status)) {
            return;
        }
        areaIds.add(area.getId());
        if (CollUtil.isEmpty(area.getChildren())) {
            return;
        }
        area.getChildren().forEach(child -> collectSelectableAreaIds(child, areaStatusMap, areaIds));
    }

    private Area getRequiredArea(Integer id) {
        Area area = AreaUtils.getArea(id);
        if (area == null) {
            throw exception(AREA_NOT_EXISTS);
        }
        return area;
    }

    private List<Area> getChinaChildren() {
        return getRequiredArea(Area.ID_CHINA).getChildren();
    }

    private Map<Integer, Integer> getAreaStatusMap() {
        return convertMap(areaConfigMapper.selectList(), AreaConfigDO::getAreaId, AreaConfigDO::getStatus);
    }

    private Set<Integer> buildIncludeAreaIds(Collection<Integer> includeAreaIds) {
        Set<Integer> areaIds = new HashSet<>();
        if (CollUtil.isEmpty(includeAreaIds)) {
            return areaIds;
        }
        includeAreaIds.stream().filter(ObjUtil::isNotNull).distinct().forEach(includeAreaId -> {
            Area area = AreaUtils.getArea(includeAreaId);
            while (area != null && !ObjUtil.equal(area.getId(), Area.ID_CHINA)
                    && !ObjUtil.equal(area.getId(), Area.ID_GLOBAL)) {
                areaIds.add(area.getId());
                area = area.getParent();
            }
        });
        return areaIds;
    }

    private List<AreaNodeRespVO> buildAreaTree(List<Area> areas, Map<Integer, Integer> areaStatusMap) {
        List<AreaNodeRespVO> result = new ArrayList<>();
        for (Area area : areas) {
            AreaNodeRespVO node = buildAreaNode(area, areaStatusMap.get(area.getId()));
            node.setChildren(buildAreaTree(area.getChildren(), areaStatusMap));
            result.add(node);
        }
        return result;
    }

    private List<AreaNodeRespVO> buildEnabledAreaTree(List<Area> areas, Map<Integer, Integer> areaStatusMap,
                                                      boolean parentEnabled, Set<Integer> includeAreaIds) {
        List<AreaNodeRespVO> result = new ArrayList<>();
        for (Area area : areas) {
            Integer status = areaStatusMap.get(area.getId());
            boolean selfEnabled = status == null || CommonStatusEnum.isEnable(status);
            boolean forceInclude = includeAreaIds.contains(area.getId());
            if (!forceInclude && (!parentEnabled || !selfEnabled)) {
                continue;
            }

            AreaNodeRespVO node = buildAreaNode(area, status);
            node.setChildren(buildEnabledAreaTree(area.getChildren(), areaStatusMap,
                    parentEnabled && selfEnabled, includeAreaIds));
            result.add(node);
        }
        return result;
    }

    private AreaNodeRespVO buildAreaNode(Area area, Integer status) {
        AreaNodeRespVO node = new AreaNodeRespVO();
        node.setId(area.getId());
        node.setName(area.getName());
        node.setStatus(status != null ? status : CommonStatusEnum.ENABLE.getStatus());
        return node;
    }

}
