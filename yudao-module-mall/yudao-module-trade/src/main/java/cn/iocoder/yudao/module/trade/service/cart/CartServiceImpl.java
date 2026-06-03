package cn.iocoder.yudao.module.trade.service.cart;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.edu.api.student.EduStudentApi;
import cn.iocoder.yudao.module.edu.api.student.dto.EduStudentOrderContextRespDTO;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.publication.api.enums.BizSceneEnum;
import cn.iocoder.yudao.module.subscription.api.order.SubscriptionOrderEligibilityApi;
import cn.iocoder.yudao.module.subscription.api.order.dto.SubscriptionOrderEligibilityReqDTO;
import cn.iocoder.yudao.module.trade.controller.app.cart.vo.*;
import cn.iocoder.yudao.module.trade.convert.cart.TradeCartConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.cart.CartDO;
import cn.iocoder.yudao.module.trade.dal.mysql.cart.CartMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SKU_NOT_EXISTS;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SKU_STOCK_NOT_ENOUGH;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.CARD_ITEM_NOT_FOUND;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_NORMAL_STUDENT_NOT_ALLOWED;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_PUBLICATION_OFFER_SKU_REQUIRED;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_PUBLICATION_STUDENT_REQUIRED;
import static java.util.Collections.emptyList;

/**
 * 购物车 Service 实现类
 *
 * // TODO 芋艿：未来优化：购物车的价格计算，支持营销信息；目前不支持的原因，前端界面需要前端 pr 支持下；例如说：会员价格；
 *
 * @author 芋道源码
 */
@Service
@Validated
public class CartServiceImpl implements CartService {

    @Resource
    private CartMapper cartMapper;

    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private ProductSkuApi productSkuApi;
    @Resource
    private EduStudentApi eduStudentApi;
    @Resource
    private SubscriptionOrderEligibilityApi subscriptionOrderEligibilityApi;

    @Override
    public Long addCart(Long userId, AppCartAddReqVO addReqVO) {
        // 校验 SKU
        Integer count = addReqVO.getCount();
        ProductSkuRespDTO sku = checkProductSku(addReqVO.getSkuId());
        ProductSpuRespDTO spu = productSpuApi.getSpu(sku.getSpuId());
        checkProductSkuStock(sku, spu, count);
        Long offerSkuId = addReqVO.getOfferSkuId();
        CartDO cart = cartMapper.selectByUserIdAndSkuIdAndStudentIdAndOfferSkuId(userId, addReqVO.getSkuId(),
                addReqVO.getStudentId(), offerSkuId);
        Long studentId = validateCartContext(userId, addReqVO.getStudentId(), offerSkuId, addReqVO.getSkuId(),
                cart == null ? count : cart.getCount() + count, spu);

        // 情况一：存在，则进行数量更新
        if (cart != null) {
            cartMapper.updateById(new CartDO().setId(cart.getId()).setSelected(true)
                    .setCount(cart.getCount() + count)
                    .setSubscriptionOfferSkuId(offerSkuId));
            return cart.getId();
        // 情况二：不存在，则进行插入
        } else {
            cart = new CartDO().setUserId(userId).setSelected(true)
                    .setSpuId(sku.getSpuId()).setSkuId(sku.getId()).setCount(count)
                    .setSubscriptionStudentId(studentId)
                    .setSubscriptionOfferSkuId(offerSkuId);
            cartMapper.insert(cart);
        }
        return cart.getId();
    }

    @Override
    public void updateCartCount(Long userId, AppCartUpdateCountReqVO updateReqVO) {
        // 校验 TradeCartDO 存在
        CartDO cart = cartMapper.selectById(updateReqVO.getId(), userId);
        if (cart == null) {
            throw exception(CARD_ITEM_NOT_FOUND);
        }
        // 校验商品 SKU
        ProductSkuRespDTO sku = checkProductSku(cart.getSkuId());
        ProductSpuRespDTO spu = productSpuApi.getSpu(sku.getSpuId());
        checkProductSkuStock(sku, spu, updateReqVO.getCount());
        validateCartContext(userId, cart.getSubscriptionStudentId(), cart.getSubscriptionOfferSkuId(),
                cart.getSkuId(), updateReqVO.getCount(), spu);

        // 更新数量
        cartMapper.updateById(new CartDO().setId(cart.getId())
                .setCount(updateReqVO.getCount()));
    }

    @Override
    public void updateCartSelected(Long userId, AppCartUpdateSelectedReqVO updateSelectedReqVO) {
        cartMapper.updateByIds(updateSelectedReqVO.getIds(), userId,
                new CartDO().setSelected(updateSelectedReqVO.getSelected()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetCart(Long userId, AppCartResetReqVO resetReqVO) {
        // 第一步：删除原本的购物项
        CartDO oldCart = cartMapper.selectById(resetReqVO.getId(), userId);
        if (oldCart == null) {
            throw exception(CARD_ITEM_NOT_FOUND);
        }
        cartMapper.deleteById(oldCart.getId());

        // 第二步：添加新的购物项
        CartDO newCart = cartMapper.selectByUserIdAndSkuIdAndStudentIdAndOfferSkuId(userId, resetReqVO.getSkuId(),
                oldCart.getSubscriptionStudentId(), oldCart.getSubscriptionOfferSkuId());
        if (newCart != null) {
            updateCartCount(userId, new AppCartUpdateCountReqVO()
                    .setId(newCart.getId()).setCount(resetReqVO.getCount()));
        } else {
            addCart(userId, new AppCartAddReqVO().setSkuId(resetReqVO.getSkuId())
                    .setCount(resetReqVO.getCount())
                    .setStudentId(oldCart.getSubscriptionStudentId())
                    .setOfferSkuId(oldCart.getSubscriptionOfferSkuId()));
        }
    }

    /**
     * 购物车删除商品
     *
     * @param userId 用户编号
     * @param ids 商品 SKU 编号的数组
     */
    @Override
    public void deleteCart(Long userId, Collection<Long> ids) {
        // 查询 TradeCartDO 列表
        List<CartDO> carts = cartMapper.selectListByIds(ids, userId);
        if (CollUtil.isEmpty(carts)) {
            return;
        }

        // 批量标记删除
        cartMapper.deleteByIds(convertSet(carts, CartDO::getId));
    }

    @Override
    public Integer getCartCount(Long userId) {
        // TODO 芋艿：需要算上 selected
        return cartMapper.selectSumByUserId(userId);
    }

    @Override
    public AppCartListRespVO getCartList(Long userId) {
        // 获得购物车的商品
        List<CartDO> carts = cartMapper.selectListByUserId(userId);
        carts.sort(Comparator.comparing(CartDO::getId).reversed());
        // 如果未空，则返回空结果
        if (CollUtil.isEmpty(carts)) {
            return new AppCartListRespVO().setGroups(emptyList())
                    .setValidList(emptyList())
                    .setInvalidList(emptyList());
        }

        // 查询 SPU、SKU 列表
        List<ProductSpuRespDTO> spus = productSpuApi.getSpuList(convertSet(carts, CartDO::getSpuId));
        List<ProductSkuRespDTO> skus = productSkuApi.getSkuList(convertSet(carts, CartDO::getSkuId));

        // 如果 SPU 被删除，则删除购物车对应的商品。延迟删除
        // 为什么不是 SKU 被删除呢？因为 SKU 被删除时，还可以通过 SPU 选择其它 SKU
        deleteCartIfSpuDeleted(carts, spus);

        // 查询学生快照并拼接数据
        Map<Long, EduStudentOrderContextRespDTO> studentMap = eduStudentApi.getOrderStudentContextMap(userId,
                convertSet(carts, CartDO::getSubscriptionStudentId, Objects::nonNull));
        return TradeCartConvert.INSTANCE.convertList(carts, spus, skus, studentMap);
    }

    private Long validateCartContext(Long userId, Long studentId, Long offerSkuId, Long skuId, Integer count,
                                     ProductSpuRespDTO spu) {
        boolean publication = spu != null && BizSceneEnum.isPublication(spu.getBizScene());
        if (publication) {
            if (studentId == null) {
                throw exception(ORDER_PUBLICATION_STUDENT_REQUIRED);
            }
            if (offerSkuId == null) {
                throw exception(ORDER_PUBLICATION_OFFER_SKU_REQUIRED);
            }
            subscriptionOrderEligibilityApi.validateOrder(new SubscriptionOrderEligibilityReqDTO()
                    .setUserId(userId)
                    .setStudentId(studentId)
                    .setOfferSkuId(offerSkuId)
                    .setSkuId(skuId)
                    .setCount(count));
            return studentId;
        }
        if (studentId != null || offerSkuId != null) {
            throw exception(ORDER_NORMAL_STUDENT_NOT_ALLOWED);
        }
        return null;
    }

    @Override
    public List<CartDO> getCartList(Long userId, Set<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return cartMapper.selectListByUserId(userId, ids);
    }

    private void deleteCartIfSpuDeleted(List<CartDO> carts, List<ProductSpuRespDTO> spus) {
        // 如果 SPU 被删除，则删除购物车对应的商品。延迟删除
        carts.removeIf(cart -> {
            if (spus.stream().noneMatch(spu -> spu.getId().equals(cart.getSpuId()))) {
                cartMapper.deleteById(cart.getId());
                return true;
            }
            return false;
        });
    }

    /**
     * 校验商品 SKU 是否合法
     * 1. 是否存在
     * 2. 是否下架
     * 3. 普通商品库存不足
     *
     * @param skuId 商品 SKU 编号
     * @param count 商品数量
     * @return 商品 SKU
     */
    private ProductSkuRespDTO checkProductSku(Long skuId) {
        ProductSkuRespDTO sku = productSkuApi.getSku(skuId);
        if (sku == null) {
            throw exception(SKU_NOT_EXISTS);
        }
        return sku;
    }

    private void checkProductSkuStock(ProductSkuRespDTO sku, ProductSpuRespDTO spu, Integer count) {
        if (spu != null && BizSceneEnum.isPublication(spu.getBizScene())) {
            return;
        }
        if (count > sku.getStock()) {
            throw exception(SKU_STOCK_NOT_ENOUGH);
        }
    }

}
