package cn.iocoder.yudao.module.product.service.sku;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuUpdateSalesCountReqDTO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.mysql.sku.ProductSkuMapper;
import cn.iocoder.yudao.module.product.service.property.ProductPropertyService;
import cn.iocoder.yudao.module.product.service.property.ProductPropertyValueService;
import cn.iocoder.yudao.module.product.service.spu.ProductSpuService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@Import(ProductSkuServiceImpl.class)
class ProductSkuSalesCountServiceTest extends BaseDbUnitTest {

    @Resource
    private ProductSkuService productSkuService;

    @Resource
    private ProductSkuMapper productSkuMapper;

    @MockitoBean
    private ProductSpuService productSpuService;
    @MockitoBean
    private ProductPropertyService productPropertyService;
    @MockitoBean
    private ProductPropertyValueService productPropertyValueService;

    @Test
    void updateSkuSalesCount_shouldNotChangeStock() {
        productSkuMapper.insert(new ProductSkuDO()
                .setId(1L)
                .setSpuId(10L)
                .setName("刊物 SKU")
                .setPrice(1000)
                .setCostPrice(800)
                .setPicUrl("https://example.com/sku.png")
                .setStock(0)
                .setSalesCount(5)
                .setStatus(0));
        ProductSkuUpdateSalesCountReqDTO reqDTO = new ProductSkuUpdateSalesCountReqDTO(singletonList(
                new ProductSkuUpdateSalesCountReqDTO.Item().setId(1L).setIncrCount(3)));

        productSkuService.updateSkuSalesCount(reqDTO);

        ProductSkuDO sku = productSkuMapper.selectById(1L);
        assertEquals(0, sku.getStock());
        assertEquals(8, sku.getSalesCount());
        verify(productSpuService).updateSpuSalesCount(argThat(spuSalesCountIncrCounts -> {
            assertEquals(1, spuSalesCountIncrCounts.size());
            assertEquals(3, spuSalesCountIncrCounts.get(10L));
            return true;
        }));
    }

}
