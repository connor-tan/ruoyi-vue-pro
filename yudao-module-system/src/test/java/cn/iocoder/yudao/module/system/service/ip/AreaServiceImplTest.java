package cn.iocoder.yudao.module.system.service.ip;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.ip.core.Area;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.system.controller.admin.ip.vo.AreaNodeRespVO;
import cn.iocoder.yudao.module.system.dal.dataobject.ip.AreaConfigDO;
import cn.iocoder.yudao.module.system.dal.mysql.ip.AreaConfigMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.AREA_NOT_EXISTS;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.AREA_NOT_SELECTABLE;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(AreaServiceImpl.class)
public class AreaServiceImplTest extends BaseDbUnitTest {

    @Resource
    private AreaServiceImpl areaService;
    @Resource
    private AreaConfigMapper areaConfigMapper;

    @Test
    public void testValidateAreaSelectable_success() {
        Area district = getSampleDistrictArea();

        areaService.validateAreaSelectable(district.getId());
    }

    @Test
    public void testValidateAreaSelectable_areaNotExists() {
        assertServiceException(() -> areaService.validateAreaSelectable(Integer.MAX_VALUE), AREA_NOT_EXISTS);
    }

    @Test
    public void testValidateAreaSelectable_areaDisabled() {
        Area district = getSampleDistrictArea();
        saveAreaConfig(district.getId(), CommonStatusEnum.DISABLE.getStatus());

        assertServiceException(() -> areaService.validateAreaSelectable(district.getId()), AREA_NOT_SELECTABLE);
    }

    @Test
    public void testValidateAreaSelectable_parentDisabled() {
        Area district = getSampleDistrictArea();
        Area parentArea = district.getParent();
        assertNotNull(parentArea);
        saveAreaConfig(parentArea.getId(), CommonStatusEnum.DISABLE.getStatus());

        assertServiceException(() -> areaService.validateAreaSelectable(district.getId()), AREA_NOT_SELECTABLE);
    }

    @Test
    public void testGetEnabledAreaTree_includeDisabledArea() {
        Area district = getSampleDistrictArea();
        Area parentArea = district.getParent();
        assertNotNull(parentArea);
        saveAreaConfig(parentArea.getId(), CommonStatusEnum.DISABLE.getStatus());

        List<Integer> includeAreaIds = List.of(district.getId());
        List<AreaNodeRespVO> areaTree = areaService.getEnabledAreaTree(includeAreaIds);

        assertTrue(containsAreaId(areaTree, district.getId()));
    }

    @Test
    public void testGetSelectableAreaIds_skipDisabledChildren() {
        Area district = getSampleDistrictArea();
        Area city = district.getParent();
        assertNotNull(city);
        saveAreaConfig(city.getId(), CommonStatusEnum.ENABLE.getStatus());
        saveAreaConfig(district.getId(), CommonStatusEnum.ENABLE.getStatus());
        Area disabledDistrict = city.getChildren().stream()
                .filter(item -> !item.getId().equals(district.getId()))
                .findFirst()
                .orElse(null);
        assertNotNull(disabledDistrict);
        saveAreaConfig(disabledDistrict.getId(), CommonStatusEnum.DISABLE.getStatus());

        Set<Integer> areaIds = areaService.getSelectableAreaIds(city.getId()).stream()
                .collect(Collectors.toSet());

        assertTrue(areaIds.contains(city.getId()));
        assertTrue(areaIds.contains(district.getId()));
        assertFalse(areaIds.contains(disabledDistrict.getId()));
    }

    private AreaConfigDO createAreaConfig(Integer areaId, Integer status) {
        AreaConfigDO areaConfig = new AreaConfigDO();
        areaConfig.setAreaId(areaId);
        areaConfig.setStatus(status);
        return areaConfig;
    }

    private void saveAreaConfig(Integer areaId, Integer status) {
        AreaConfigDO areaConfig = areaConfigMapper.selectByAreaId(areaId);
        if (areaConfig == null) {
            areaConfigMapper.insert(createAreaConfig(areaId, status));
            return;
        }
        areaConfig.setStatus(status);
        areaConfig.clean();
        areaConfigMapper.updateById(areaConfig);
    }

    private Area getSampleDistrictArea() {
        Area china = AreaUtils.getArea(Area.ID_CHINA);
        assertNotNull(china);
        for (Area province : china.getChildren()) {
            for (Area city : province.getChildren()) {
                if (!city.getChildren().isEmpty()) {
                    return city.getChildren().get(0);
                }
            }
        }
        throw new IllegalStateException("找不到可用于测试的区县地区");
    }

    private boolean containsAreaId(List<AreaNodeRespVO> areas, Integer targetId) {
        for (AreaNodeRespVO area : areas) {
            if (area.getId().equals(targetId)) {
                return true;
            }
            if (area.getChildren() != null && containsAreaId(area.getChildren(), targetId)) {
                return true;
            }
        }
        return false;
    }

}
