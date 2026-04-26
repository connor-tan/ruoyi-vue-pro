package cn.iocoder.yudao.module.trade.dal.dataobject.order;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.aftersale.AfterSaleDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.cart.CartDO;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderItemAfterSaleStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 交易订单项 DO
 *
 * @author 芋道源码
 */
@TableName(value = "trade_order_item", autoResultMap = true)
@KeySequence("trade_order_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class TradeOrderItemDO extends BaseDO {

    // ========== 订单项基本信息 ==========
    /**
     * 编号
     */
    private Long id;
    /**
     * 用户编号
     *
     * 关联 MemberUserDO 的 id 编号
     */
    private Long userId;
    /**
     * 订单编号
     *
     * 关联 {@link TradeOrderDO#getId()}
     */
    private Long orderId;
    /**
     * 配送组编号
     */
    private Long deliveryId;
    /**
     * 购物车项编号
     *
     * 关联 {@link CartDO#getId()}
     */
    private Long cartId;

    // ========== 商品基本信息; 冗余较多字段，减少关联查询 ==========
    /**
     * 商品 SPU 编号
     *
     * 关联 ProductSkuDO 的 spuId 编号
     */
    private Long spuId;
    /**
     * 商品 SPU 名称
     *
     * 冗余 ProductSkuDO 的 spuName 编号
     */
    private String spuName;
    /**
     * 商品 SKU 编号
     *
     * 关联 ProductSkuDO 的 id 编号
     */
    private Long skuId;
    /**
     * 属性数组，JSON 格式
     *
     * 冗余 ProductSkuDO 的 properties 字段
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Property> properties;
    /**
     * 商品图片
     */
    private String picUrl;
    /**
     * 购买数量
     */
    private Integer count;
    /**
     * 是否评价
     *
     * true - 已评价
     * false - 未评价
     */
    private Boolean commentStatus;

    // ========== 价格 + 支付基本信息 ==========

    /**
     * 商品原价（单），单位：分
     *
     * 对应 ProductSkuDO 的 price 字段
     * 对应 taobao 的 order.price 字段
     */
    private Integer price;
    /**
     * 优惠金额（总），单位：分
     *
     * 对应 taobao 的 order.discount_fee 字段
     */
    private Integer discountPrice;
    /**
     * 运费金额（总），单位：分
     */
    private Integer deliveryPrice;
    /**
     * 订单调价（总），单位：分
     *
     * 正数，加价；负数，减价
     */
    private Integer adjustPrice;
    /**
     * 应付金额（总），单位：分
     *
     * = {@link #price} * {@link #count}
     * - {@link #couponPrice}
     * - {@link #pointPrice}
     * - {@link #discountPrice}
     * + {@link #deliveryPrice}
     * + {@link #adjustPrice}
     * - {@link #vipPrice}
     */
    private Integer payPrice;

    // ========== 营销基本信息 ==========

    /**
     * 优惠劵减免金额，单位：分
     *
     * 对应 taobao 的 trade.coupon_fee 字段
     */
    private Integer couponPrice;
    /**
     * 积分抵扣的金额，单位：分
     *
     * 对应 taobao 的 trade.point_fee 字段
     */
    private Integer pointPrice;
    /**
     * 使用的积分
     *
     * 目的：用于后续取消或者售后订单时，需要归还赠送
     */
    private Integer usePoint;
    /**
     * 赠送的积分
     *
     * 目的：用于后续取消或者售后订单时，需要扣减赠送
     */
    private Integer givePoint;
    /**
     * VIP 减免金额，单位：分
     */
    private Integer vipPrice;

    // ========== 售后基本信息 ==========

    /**
     * 售后单编号
     *
     * 关联 {@link AfterSaleDO#getId()} 字段
     */
    private Long afterSaleId;
    /**
     * 售后状态
     *
     * 枚举 {@link TradeOrderItemAfterSaleStatusEnum}
     */
    private Integer afterSaleStatus;

    // ========== 订刊快照 ==========

    /**
     * 订刊学生编号
     */
    private Long subscriptionStudentId;
    /**
     * 订刊学生名称快照
     */
    private String subscriptionStudentNameSnapshot;
    /**
     * 订刊学校编号
     */
    private Long subscriptionSchoolId;
    /**
     * 订刊学校名称快照
     */
    private String subscriptionSchoolNameSnapshot;
    /**
     * 订刊班级编号
     */
    private Long subscriptionClassId;
    /**
     * 订刊班级名称快照
     */
    private String subscriptionClassNameSnapshot;
    /**
     * 订刊解析年级编号
     */
    private Long subscriptionGradeCatalogId;
    /**
     * 订刊解析年级名称快照
     */
    private String subscriptionGradeNameSnapshot;
    /**
     * 订刊窗口编号
     */
    private Long subscriptionWindowId;
    /**
     * 订刊窗口名称快照
     */
    private String subscriptionWindowNameSnapshot;
    /**
     * 订刊目标学年开始年份
     */
    private Integer subscriptionTargetYearStart;
    /**
     * 订刊目标学年结束年份
     */
    private Integer subscriptionTargetYearEnd;
    /**
     * 订刊目标周期
     */
    private String subscriptionTargetPeriod;
    /**
     * 订刊窗口刊物编号
     *
     * @deprecated 新模型使用 {@link #subscriptionOfferId}
     */
    @Deprecated
    private Long subscriptionWindowSpuId;
    /**
     * 订刊窗口 SKU 编号
     *
     * @deprecated 新模型使用 {@link #subscriptionOfferSkuId}
     */
    @Deprecated
    private Long subscriptionWindowSkuId;
    /**
     * 订刊窗口刊物编号（offer）
     */
    private Long subscriptionOfferId;
    /**
     * 订刊窗口 SKU 编号（offerSku）
     */
    private Long subscriptionOfferSkuId;
    /**
     * 订刊可见原因
     */
    private String subscriptionVisibilityReason;
    /**
     * 订刊命中特殊规则编号
     */
    private Long subscriptionMatchedRuleId;
    /**
     * 是否突破刊物商品适用年级
     */
    private Boolean subscriptionGradeApplicabilityOverride;

    /**
     * 商品属性
     */
    @Data
    public static class Property implements Serializable {

        /**
         * 属性编号
         *
         * 关联 ProductPropertyDO 的 id 编号
         */
        private Long propertyId;
        /**
         * 属性名字
         *
         * 关联 ProductPropertyDO 的 name 字段
         */
        private String propertyName;

        /**
         * 属性值编号
         *
         * 关联 ProductPropertyValueDO 的 id 编号
         */
        private Long valueId;
        /**
         * 属性值名字
         *
         * 关联 ProductPropertyValueDO 的 name 字段
         */
        private String valueName;

    }

}
