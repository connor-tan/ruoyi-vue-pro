package cn.iocoder.yudao.module.product.dal.mysql.spu;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.product.controller.app.spu.vo.AppProductSpuPageReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.publication.api.enums.BizSceneEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductSpuMapperMultiCategoryTest extends BaseDbUnitTest {

    @Resource
    private ProductSpuMapper productSpuMapper;
    @Resource
    private DataSource dataSource;

    @Test
    void selectAppPage_filtersByIds() {
        ProductSpuDO matchedSpu = productSpuMapper.selectById(createSpu("matched"));
        createSpu("unmatched");

        AppProductSpuPageReqVO reqVO = new AppProductSpuPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        reqVO.setIds(List.of(matchedSpu.getId()));
        PageResult<ProductSpuDO> pageResult = productSpuMapper.selectPage(reqVO, Collections.emptySet());

        assertEquals(1, pageResult.getTotal());
        assertEquals(matchedSpu.getId(), pageResult.getList().get(0).getId());
    }

    private Long createSpu(String name) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("""
                        INSERT INTO product_spu (name, keyword, introduction, description, bar_code, biz_scene,
                            pic_url, unit, sort, status, spec_type, price, market_price, cost_price, stock,
                            recommend_hot, recommend_benefit, recommend_best, recommend_new, recommend_good,
                            give_integral, sub_commission_type, sales_count, virtual_sales_count, browse_count)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                name, name, name, name, name, BizSceneEnum.NORMAL.getCode(), "https://example.com/spu.png",
                1, 1, ProductSpuStatusEnum.ENABLE.getStatus(), false, 100, 100, 100, 10,
                false, false, false, false, false, 0, false, 0, 0, 0);
        return jdbcTemplate.queryForObject("SELECT MAX(id) FROM product_spu", Long.class);
    }

}
