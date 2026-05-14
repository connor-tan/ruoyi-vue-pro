package cn.iocoder.yudao.module.subscription.dal.mysql;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.publication.api.enums.BizSceneEnum;
import cn.iocoder.yudao.module.subscription.controller.admin.offer.vo.SubscriptionOfferAvailablePageReqVO;
import cn.iocoder.yudao.module.subscription.controller.admin.offer.vo.SubscriptionOfferAvailableRespVO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubscriptionWindowOfferMapperTest extends BaseDbUnitTest {

    @Resource
    private SubscriptionWindowOfferMapper offerMapper;
    @Resource
    private DataSource dataSource;

    @Test
    void selectAvailableCandidates_shouldTreatSpuStatusOneAsCanAdd() {
        insertPublicationCandidate(100L, ProductSpuStatusEnum.ENABLE.getStatus(), CommonStatusEnum.ENABLE.getStatus());

        List<SubscriptionOfferAvailableRespVO> candidates = offerMapper.selectAvailableCandidates(candidateReqVO(), 0, 10);

        assertEquals(1, candidates.size());
        SubscriptionOfferAvailableRespVO candidate = candidates.get(0);
        assertEquals(100L, candidate.getProductSpuId());
        assertEquals("CAN_ADD", candidate.getCandidateStatus());
        assertEquals(1, candidate.getMatchedSkuCount());
        assertEquals(1, candidate.getEnabledSkuCount());
    }

    @Test
    void selectAvailableCandidates_shouldNotTreatSpuStatusZeroAsCanAdd() {
        insertPublicationCandidate(101L, ProductSpuStatusEnum.DISABLE.getStatus(), CommonStatusEnum.ENABLE.getStatus());

        List<SubscriptionOfferAvailableRespVO> candidates = offerMapper.selectAvailableCandidates(candidateReqVO(), 0, 10);

        assertEquals(0, candidates.size());
    }

    private SubscriptionOfferAvailablePageReqVO candidateReqVO() {
        SubscriptionOfferAvailablePageReqVO reqVO = new SubscriptionOfferAvailablePageReqVO();
        reqVO.setWindowId(1L);
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        reqVO.setCandidateStatus("CAN_ADD");
        return reqVO;
    }

    private void insertPublicationCandidate(Long spuId, Integer spuStatus, Integer skuStatus) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("""
                        INSERT INTO product_spu (id, name, biz_scene, pic_url, price, stock, sort, status)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                spuId, "测试刊物" + spuId, BizSceneEnum.PUBLICATION.getCode(),
                "https://example.com/spu.png", 1000, 10, 1, spuStatus);
        jdbcTemplate.update("""
                        INSERT INTO product_publication_spu_ext
                            (spu_id, publisher_id, publication_type_id, issue_cycle)
                        VALUES (?, ?, ?, ?)
                        """, spuId, 10L, 20L, "MONTHLY");
        jdbcTemplate.update("""
                        INSERT INTO product_spu_category_rel (spu_id, category_id, sort)
                        VALUES (?, ?, ?)
                        """, spuId, 30L, 1);
        jdbcTemplate.update("""
                        INSERT INTO product_category (id, name)
                        VALUES (?, ?)
                        """, 30L, "刊物分类");
        jdbcTemplate.update("""
                        INSERT INTO product_publisher (id, name)
                        VALUES (?, ?)
                        """, 10L, "测试出版社");
        jdbcTemplate.update("""
                        INSERT INTO product_publication_type (id, name)
                        VALUES (?, ?)
                        """, 20L, "测试刊型");
        jdbcTemplate.update("""
                        INSERT INTO product_sku (id, spu_id, status)
                        VALUES (?, ?, ?)
                        """, spuId + 1000, spuId, skuStatus);
        jdbcTemplate.update("""
                        INSERT INTO product_publication_sku_grade_rel (sku_id, grade_catalog_id)
                        VALUES (?, ?)
                        """, spuId + 1000, 40L);
    }

}
