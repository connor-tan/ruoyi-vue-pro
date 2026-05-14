package cn.iocoder.yudao.module.trade.dal.dataobject.order;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 交易订单配送组 DO
 */
@TableName("trade_order_delivery")
@KeySequence("trade_order_delivery_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TradeOrderDeliveryDO extends BaseDO {

    /**
     * 配送组编号
     */
    private Long id;
    /**
     * 订单编号
     */
    private Long orderId;
    /**
     * 配送方式
     *
     * 枚举 {@link DeliveryTypeEnum}
     */
    private Integer deliveryType;
    /**
     * 配送状态
     *
     * 枚举 {@link TradeOrderStatusEnum}
     */
    private Integer status;
    /**
     * 商品数量
     */
    private Integer productCount;
    /**
     * 配送组实付金额
     */
    private Integer payPrice;
    /**
     * 配送组运费
     */
    private Integer deliveryPrice;
    /**
     * 物流公司编号
     */
    private Long logisticsId;
    /**
     * 物流单号
     */
    private String logisticsNo;
    /**
     * 发货时间
     */
    private LocalDateTime deliveryTime;
    /**
     * 收货时间
     */
    private LocalDateTime receiveTime;
    /**
     * 收件人名称
     */
    private String receiverName;
    /**
     * 收件人手机
     */
    private String receiverMobile;
    /**
     * 收件地区编号
     */
    private Integer receiverAreaId;
    /**
     * 收件详细地址
     */
    private String receiverDetailAddress;
    /**
     * 自提门店编号
     */
    private Long pickUpStoreId;
    /**
     * 自提核销码
     */
    private String pickUpVerifyCode;
    /**
     * 学校编号
     */
    private Long schoolId;
    /**
     * 学校名称快照
     */
    private String schoolNameSnapshot;
    /**
     * 学校地址快照
     */
    private String schoolAddressSnapshot;
    /**
     * 学校配送仓库编号
     */
    private Long warehouseId;
    /**
     * 学校配送仓库名称快照
     */
    private String warehouseNameSnapshot;
    /**
     * 学校配送仓库地址快照
     */
    private String warehouseAddressSnapshot;
    /**
     * 联系人
     */
    private String contactName;
    /**
     * 联系电话
     */
    private String contactMobile;
    /**
     * 业务场景
     */
    private String bizScene;
    /**
     * 订刊学生编号
     */
    private Long studentId;
    /**
     * 订刊学生名称快照
     */
    private String studentNameSnapshot;
    /**
     * 订刊班级编号
     */
    private Long classId;
    /**
     * 订刊班级名称快照
     */
    private String classNameSnapshot;
    /**
     * 订刊年级目录编号
     */
    private Long gradeCatalogId;
    /**
     * 订刊年级名称快照
     */
    private String gradeNameSnapshot;

}
