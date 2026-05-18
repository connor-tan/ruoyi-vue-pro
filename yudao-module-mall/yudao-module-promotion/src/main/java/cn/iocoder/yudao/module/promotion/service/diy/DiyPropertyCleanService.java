package cn.iocoder.yudao.module.promotion.service.diy;

/**
 * 装修属性清理 Service 接口
 *
 * @author Connor
 */
public interface DiyPropertyCleanService {

    /**
     * 清理装修属性中的无效商品引用
     *
     * @param property 装修属性 JSON
     * @return 清理后的装修属性 JSON
     */
    String cleanInvalidSpuIds(String property);

    /**
     * 从所有装修页面中移除指定商品引用
     *
     * @param spuId 商品 SPU 编号
     * @return 被更新的装修页面数量
     */
    int removeSpuIdFromAllPages(Long spuId);

}
