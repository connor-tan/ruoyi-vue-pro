package cn.iocoder.yudao.module.promotion.service.diy;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.product.service.spu.ProductSpuService;
import cn.iocoder.yudao.module.promotion.dal.dataobject.diy.DiyPageDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.diy.DiyPageMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

/**
 * {@link DiyPropertyCleanServiceImpl} 的单元测试类
 *
 * @author Connor
 */
@ExtendWith(MockitoExtension.class)
class DiyPropertyCleanServiceImplTest {

    @InjectMocks
    private DiyPropertyCleanServiceImpl diyPropertyCleanService;

    @Mock
    private ProductSpuService productSpuService;
    @Mock
    private DiyPageMapper diyPageMapper;

    @Test
    void cleanInvalidSpuIds_removesInvalidAndDuplicateIds() {
        // 准备参数
        String property = """
                {"components":[
                  {"id":"ProductList","property":{"spuIds":[2,1,2,3]}},
                  {"id":"ProductCard","property":{"spuIds":[3,4]}},
                  {"id":"MagicCube","property":{"spuIds":[1]}}
                ]}
                """;
        when(productSpuService.getSpuList(anyCollection())).thenReturn(List.of(
                new ProductSpuDO().setId(2L).setStatus(ProductSpuStatusEnum.ENABLE.getStatus()),
                new ProductSpuDO().setId(3L).setStatus(ProductSpuStatusEnum.DISABLE.getStatus()),
                new ProductSpuDO().setId(4L).setStatus(ProductSpuStatusEnum.ENABLE.getStatus())));

        // 调用
        String cleaned = diyPropertyCleanService.cleanInvalidSpuIds(property);

        // 断言
        JsonNode components = JsonUtils.parseTree(cleaned).path("components");
        assertEquals("[2]", components.get(0).path("property").path("spuIds").toString());
        assertEquals("[4]", components.get(1).path("property").path("spuIds").toString());
        assertEquals("[1]", components.get(2).path("property").path("spuIds").toString());
    }

    @Test
    void cleanInvalidSpuIds_invalidJson() {
        assertThrows(ServiceException.class, () -> diyPropertyCleanService.cleanInvalidSpuIds("{"));
    }

    @Test
    void removeSpuIdFromAllPages_success() {
        // 准备参数
        String property = """
                {"components":[
                  {"id":"ProductList","property":{"spuIds":[2,1]}},
                  {"id":"ProductCard","property":{"spuIds":[1,3]}}
                ]}
                """;
        when(diyPageMapper.selectList()).thenReturn(List.of(
                new DiyPageDO().setId(10L).setProperty(property),
                new DiyPageDO().setId(11L).setProperty("{\"components\":[]}")));

        // 调用
        int updateCount = diyPropertyCleanService.removeSpuIdFromAllPages(1L);

        // 断言
        assertEquals(1, updateCount);
        ArgumentCaptor<DiyPageDO> captor = ArgumentCaptor.forClass(DiyPageDO.class);
        verify(diyPageMapper).updateById(captor.capture());
        DiyPageDO updateObj = captor.getValue();
        assertEquals(10L, updateObj.getId());
        JsonNode components = JsonUtils.parseTree(updateObj.getProperty()).path("components");
        assertEquals("[2]", components.get(0).path("property").path("spuIds").toString());
        assertEquals("[3]", components.get(1).path("property").path("spuIds").toString());
    }

}
